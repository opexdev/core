package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.common.utils.CacheManager
import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.model.WalletType
import co.nilin.opex.wallet.core.spi.WalletDataManager
import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepositoryV2
import co.nilin.opex.wallet.ports.postgres.dao.WalletRepository
import co.nilin.opex.wallet.ports.postgres.model.CurrencyModel
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

@Component
class WalletDataManagerImpl(
    private val walletRepository: WalletRepository,
    private val totalAssetsSnapshotImpl: TotalAssetsSnapshotImpl,
    private val currencyRepositoryV2: CurrencyRepositoryV2,
    private val objectMapper: ObjectMapper,
    private val cacheManager: CacheManager<String, BigDecimal>,
    @Value("\${app.zone-offset}") private val zoneOffsetString: String,
    ) : WalletDataManager {

    override suspend fun findWalletDataByCriteria(
        uuid: String?,
        walletType: WalletType?,
        currency: String?,
        excludeSystem: Boolean,
        limit: Int,
        offset: Int
    ): List<WalletData> {
        val currency = currencyRepositoryV2.fetchCurrency(symbol = currency)?.awaitFirstOrNull()
        return (if (!excludeSystem) walletRepository.findWalletDataByCriteria(
            uuid,
            walletType,
            currency?.symbol,
            limit,
            offset
        ) else
            walletRepository.findWalletDataByCriteriaExcludeSystem(
                uuid,
                walletType,
                currency?.symbol,
                limit,
                offset
            )).collectList().awaitFirstOrElse { emptyList() }
    }

    override suspend fun findWalletDataByCriteria(
        uuid: String?,
        currency: String?,
        excludeSystem: Boolean,
        limit: Int,
        offset: Int
    ): List<WalletDataResponse> {
        return walletRepository.findWalletDataByCriteria(
            uuid,
            currency,
            excludeSystem,
            limit,
            offset
        ).map { raw ->
            val walletsList = try {
                objectMapper.readValue(
                    raw.wallets,
                    object : TypeReference<List<WalletCurrencyData>>() {}
                )
            } catch (e: Exception) {
                emptyList<WalletCurrencyData>()
            }
            WalletDataResponse(
                uuid = raw.uuid,
                title = raw.title,
                wallets = walletsList
            )
        }.collectList().awaitFirstOrElse { emptyList() }
    }

    override suspend fun findSystemWalletsTotal(): List<WalletTotal> {
        return walletRepository.findSystemWalletsTotal().collectList().awaitFirstOrElse { emptyList() }
    }

    override suspend fun findUserWalletsTotal(): List<WalletTotal>? {
        val allCurrencies = currencyRepositoryV2.fetchSemiCurrencies()?.map(CurrencyModel::symbol)
        val allDepositedCurrency =
            walletRepository.findUserWalletsTotal().collectList().awaitFirstOrElse { emptyList() }
        return allCurrencies?.map { c ->
            WalletTotal(c, (allDepositedCurrency.filter { it.currency == c }?.firstOrNull()?.balance) ?: 0.0)
        }?.collectList()?.awaitFirstOrNull()
    }

    override suspend fun getLastDaysBalance(
        userId: String,
        quoteCurrency: String?,
        n: Int
    ): List<DailyAmount> {
        val today = LocalDate.now(ZoneOffset.of(zoneOffsetString))
        val dates = (0..n - 1).map { today.minusDays(it.toLong()) }

        val result = mutableMapOf<LocalDate, BigDecimal>()
        val missingDates = mutableListOf<LocalDate>()

        for (date in dates) {
            val cacheKey = "trade:daily:$userId:$date"
            val cached = cacheManager.get(cacheKey)

            if (cached != null) {
                result[date] = cached
            } else {
                missingDates.add(date)
            }
        }

        if (missingDates.isNotEmpty()) {
            val startDate = missingDates.minOrNull()!!

            val dbData = totalAssetsSnapshotImpl.getLastDaysBalance(userId, startDate, quoteCurrency)
                .stream().collect(Collectors.toMap(DailyAmount::date, DailyAmount::totalAmount));

            for (date in missingDates) {
                val value = dbData[date] ?: BigDecimal.ZERO
                val (ttl, unit) = ttlFor(date, today)
                val cacheKey = "trade:daily:$userId:$date"

                cacheManager.put(cacheKey, value, ttl, unit)
                result[date] = value
            }
        }

        return result
            .map { DailyAmount(it.key, it.value) }
            .sortedBy { it.date }

    }


    private fun ttlFor(date: LocalDate, today: LocalDate): Pair<Long, TimeUnit> =
        if (date == today) {
            15L to TimeUnit.MINUTES
        } else {
            100L to TimeUnit.DAYS
        }
}