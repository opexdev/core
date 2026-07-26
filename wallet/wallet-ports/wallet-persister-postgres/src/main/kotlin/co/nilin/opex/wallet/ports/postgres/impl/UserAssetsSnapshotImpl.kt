package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.inout.DailyAmount
import co.nilin.opex.wallet.core.model.CurrencyAssetsSnapshot
import co.nilin.opex.wallet.core.model.TotalAssetsSnapshot
import co.nilin.opex.wallet.core.model.UserDetailAssetsSnapshot
import co.nilin.opex.wallet.core.spi.UserAssetsSnapshotManager
import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepositoryV2
import co.nilin.opex.wallet.ports.postgres.dao.DetailAssetsSnapshotRepository
import co.nilin.opex.wallet.ports.postgres.dao.TotalAssetsSnapshotRepository
import co.nilin.opex.wallet.ports.postgres.util.RedisCacheHelper
import co.nilin.opex.wallet.ports.postgres.util.toTotalAssetsSnapshot
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.ZoneOffset

@Service
class UserAssetsSnapshotImpl(
    private val currencyRepository: CurrencyRepositoryV2,
    private val totalAssetsSnapshotRepository: TotalAssetsSnapshotRepository,
    private val detailAssetsSnapshotRepository: DetailAssetsSnapshotRepository,
    private val objectMapper: ObjectMapper,
    private val redisCacheHelper: RedisCacheHelper,
    @Value("\${app.snapshot-currency}")
    private val snapshotCurrency: String,
    @Value("\${app.zone-offset}") private val zoneOffsetString: String
) : UserAssetsSnapshotManager {

    private val logger = LoggerFactory.getLogger(UserAssetsSnapshotImpl::class.java)

    override suspend fun createTotalAssetsSnapshot() {
        val start = System.currentTimeMillis()
        logger.info("Starting total assets snapshot creation...")
        val currency = currencyRepository.fetchCurrency(symbol = snapshotCurrency)?.awaitFirstOrNull()
            ?: throw OpexError.CurrencyNotFound.exception()
        totalAssetsSnapshotRepository.createSnapshotsDirectly(currency.symbol, currency.precision.toInt())
            .awaitFirstOrNull()
        val end = System.currentTimeMillis()
        logger.info("Total assets snapshot creation finished in {} ms", (end - start))
    }

    override suspend fun createDetailAssetsSnapshot() {
        val start = System.currentTimeMillis()
        logger.info("Starting detail assets snapshot creation...")
        val currency = currencyRepository.fetchCurrency(symbol = snapshotCurrency)?.awaitFirstOrNull()
            ?: throw OpexError.CurrencyNotFound.exception()
        detailAssetsSnapshotRepository
            .createDetailSnapshotsDirectly(currency.symbol, currency.precision.toInt())
            .awaitFirstOrNull()
        val end = System.currentTimeMillis()
        logger.info("Detail assets snapshot creation finished in {} ms", (end - start))
    }

    override suspend fun getUserLastTotalAssetsSnapshot(
        uuid: String
    ): TotalAssetsSnapshot? {
        return totalAssetsSnapshotRepository.findLastSnapshotByUuid(uuid).awaitFirstOrNull()?.toTotalAssetsSnapshot()
    }

    override suspend fun getUsersLastDetailAssetsSnapshot(
        limit: Int,
        offset: Int
    ): List<UserDetailAssetsSnapshot> {
        val key = "users-detail-assets:$limit-$offset"
        redisCacheHelper.get<List<UserDetailAssetsSnapshot>>(key)?.let {
            return it
        }
        val result = detailAssetsSnapshotRepository
            .findAllLatestSnapshots(limit, offset)
            .map { raw ->
                UserDetailAssetsSnapshot(
                    uuid = raw.uuid.substringAfterLast('-'),
                    currencySnapshots = objectMapper.readValue(
                        raw.currencySnapshots,
                        object : TypeReference<List<CurrencyAssetsSnapshot>>() {}
                    ),
                    totalAmount = raw.totalAmount,
                    quoteCurrency = raw.quoteCurrency,
                    snapshotDate = raw.snapshotDate
                )
            }
            .collectList()
            .awaitFirstOrElse { emptyList() }
        redisCacheHelper.put(key, result)
        return result
    }

    override suspend fun getLastDaysBalance(
        userId: String,
        startDate: LocalDate?,
        quatCurrency: String?,
        lastDays: Long
    ): List<DailyAmount> {

        val startDate = startDate ?: LocalDate
            .now(ZoneOffset.of(zoneOffsetString))
            .minusDays(lastDays)

        return totalAssetsSnapshotRepository.findDailyBalance(userId, startDate, quatCurrency ?: snapshotCurrency)
            .map {
                DailyAmount(
                    date = it.date,
                    totalAmount = it.totalAmount
                )
            }
            .collectList()
            .awaitSingle()
    }
}
