package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.api.FeeCalculator
import co.nilin.opex.accountant.core.model.*
import co.nilin.opex.accountant.core.spi.FeeConfigService
import co.nilin.opex.accountant.core.spi.JsonMapper
import co.nilin.opex.accountant.core.spi.UserVolumePersister
import co.nilin.opex.accountant.core.spi.WalletProxy
import co.nilin.opex.accountant.core.utils.CacheManager
import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.*
import java.util.concurrent.TimeUnit

@Component
class FeeCalculatorImpl(
    private val walletProxy: WalletProxy,
    private val feeConfigService: FeeConfigService,
    private val userVolumePersister: UserVolumePersister,
    private val cacheManager: CacheManager<String, FeeConfig>,
    @Value("\${app.address}") private val platformAddress: String,
    private val jsonMapper: JsonMapper
) : FeeCalculator {

    private val logger = LoggerFactory.getLogger(FeeCalculatorImpl::class.java)

    private fun ttlUntilNextDay1amMillis(zone: ZoneId = ZoneId.of("GMT+03:30")): Long {
        val now = ZonedDateTime.now(zone)
        val target = now.toLocalDate().plusDays(1).atTime(1, 0).atZone(zone)
        val ttl = Duration.between(now, target).toMillis()
        return if (ttl > 0) ttl else Duration.ofHours(1).toMillis()
    }

    override suspend fun getUserFee(uuid: String): FeeConfig {
        val cached = cacheManager.get(uuid)
        if (cached != null) {
            return cached
        }

        val totalAssets = walletProxy.getUserTotalAssets(uuid)
        val userVolumeData = userVolumePersister.getUserVolumeData(uuid, LocalDate.now().minusMonths(1L))

        val feeConfig = feeConfigService.loadMatchingFeeConfig(
            totalAssets?.totalUSDT ?: BigDecimal.ZERO,
            userVolumeData?.valueUSDT ?: BigDecimal.ZERO
        )
        val ttl = ttlUntilNextDay1amMillis()
        cacheManager.put(uuid, feeConfig, ttl, TimeUnit.MILLISECONDS)
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
}