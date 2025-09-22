package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.api.FeeCalculator
import co.nilin.opex.accountant.core.model.*
import co.nilin.opex.accountant.core.spi.FeeConfigService
import co.nilin.opex.accountant.core.spi.JsonMapper
import co.nilin.opex.accountant.core.spi.UserVolumePersister
import co.nilin.opex.accountant.core.spi.WalletProxy
import co.nilin.opex.common.utils.CacheManager
import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

@Component
class FeeCalculatorImpl(
    private val walletProxy: WalletProxy,
    private val feeConfigService: FeeConfigService,
    private val userVolumePersister: UserVolumePersister,
    @Qualifier("appCacheManager") private val cacheManager: CacheManager<String, UserFee>,
    @Value("\${app.address}") private val platformAddress: String,
    @Value("\${app.zone-offset}") private val zoneOffsetString: String,
    @Value("\${app.trade-volume-calculation-currency}")
    private val tradeVolumeCalculationCurrency: String,
    private val jsonMapper: JsonMapper
) : FeeCalculator {

    private val logger = LoggerFactory.getLogger(FeeCalculatorImpl::class.java)

    override suspend fun getUserFee(uuid: String): UserFee {
        val cached = cacheManager.get(uuid)
        if (cached != null) {
            return cached
        }

        val totalAssets = walletProxy.getUserTotalAssets(uuid)
        val userVolumeData = userVolumePersister.getUserTotalTradeVolume(
            uuid,
            LocalDateTime.now().atOffset(ZoneOffset.of(zoneOffsetString)).toLocalDate().minusMonths(1L),
            tradeVolumeCalculationCurrency
        )

        val feeConfig = feeConfigService.loadMatchingFeeConfig(
            totalAssets?.totalAmount ?: BigDecimal.ZERO,
            userVolumeData ?: BigDecimal.ZERO
        )
        val remainingMillis = remainingMillisUntil1AM()
        cacheManager.put(uuid, feeConfig, remainingMillis, TimeUnit.MILLISECONDS)
        return feeConfig
    }

    override suspend fun createFeeActions(
        trade: TradeEvent,
        makerOrder: Order,
        takerOrder: Order,
        makerParentFA: FinancialAction?,
        takerParentFA: FinancialAction?
    ): FeeFinancialActions {
        logger.info("Start fee calculation for trade ${trade.takerUuid}")

        val makerMatchedAmount = if (makerOrder.isAsk()) {
            trade.matchedQuantity.toBigDecimal().multiply(makerOrder.leftSideFraction)
        } else {
            trade.matchedQuantity.toBigDecimal()
                .multiply(makerOrder.leftSideFraction)
                .multiply(trade.makerPrice.toBigDecimal())
                .multiply(makerOrder.rightSideFraction)
        }

        val takerMatchedAmount = if (takerOrder.isAsk()) {
            trade.matchedQuantity.toBigDecimal().multiply(takerOrder.leftSideFraction)
        } else {
            trade.matchedQuantity.toBigDecimal()
                .multiply(takerOrder.leftSideFraction)
                .multiply(trade.makerPrice.toBigDecimal())
                .multiply(takerOrder.rightSideFraction)
        }

        //calculate maker fee
        val makerFeeAction = FinancialAction(
            makerParentFA,
            TradeEvent::class.simpleName!!,
            trade.takerOuid,
            if (takerOrder.isAsk()) trade.pair.leftSideName else trade.pair.rightSideName,
            takerMatchedAmount.multiply(makerOrder.makerFee),
            trade.makerUuid,
            WalletType.MAIN,
            platformAddress,
            WalletType.EXCHANGE,
            LocalDateTime.now(),
            FinancialActionCategory.FEE
        )
        logger.info("Trade event makerFeeAction: uuid=${makerFeeAction.uuid}")

        //calculate taker fee
        val takerFeeAction = FinancialAction(
            takerParentFA,
            TradeEvent::class.simpleName!!,
            trade.makerOuid,
            if (makerOrder.isAsk()) trade.pair.leftSideName else trade.pair.rightSideName,
            makerMatchedAmount.multiply(takerOrder.takerFee),
            trade.takerUuid,
            WalletType.MAIN,
            platformAddress,
            WalletType.EXCHANGE,
            LocalDateTime.now(),
            FinancialActionCategory.FEE
        )
        logger.info("Trade event takerFeeAction: uuid=${takerFeeAction.uuid}")

        return FeeFinancialActions(makerFeeAction, takerFeeAction)
    }

    private fun createMap(tradeEvent: TradeEvent, order: Order): Map<String, Any> {
        val orderMap: Map<String, Any> = jsonMapper.toMap(order)
        val eventMap: Map<String, Any> = jsonMapper.toMap(tradeEvent)
        return orderMap + eventMap
    }

    private fun remainingMillisUntil1AM(): Long {
        val now = LocalDateTime.now().atOffset(ZoneOffset.of(zoneOffsetString))
        val target =
            LocalDateTime.now().atOffset(ZoneOffset.of(zoneOffsetString)).plusDays(1).withHour(1)
        val remainingMillis = Duration.between(now, target).toMillis()
        return if (remainingMillis > 0) remainingMillis else Duration.ofHours(1).toMillis()
    }
}