package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.inout.DailyAmount
import co.nilin.opex.wallet.core.model.TotalAssetsSnapshot
import co.nilin.opex.wallet.core.spi.TotalAssetsSnapshotManager
import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepositoryV2
import co.nilin.opex.wallet.ports.postgres.dao.TotalAssetsSnapshotRepository
import co.nilin.opex.wallet.ports.postgres.util.toTotalAssetsSnapshot
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.ZoneOffset

@Service
class TotalAssetsSnapshotImpl(
    private val currencyRepository: CurrencyRepositoryV2,
    private val totalAssetsSnapshotRepository: TotalAssetsSnapshotRepository,
    @Value("\${app.snapshot-currency}")
    private val snapshotCurrency: String,
    @Value("\${app.zone-offset}") private val zoneOffsetString: String
) : TotalAssetsSnapshotManager {

    private val logger = LoggerFactory.getLogger(TotalAssetsSnapshotImpl::class.java)

    override suspend fun createSnapshot() {
        val start = System.currentTimeMillis()
        logger.info("Starting snapshot creation...")
        val currency = currencyRepository.fetchCurrency(symbol = snapshotCurrency)?.awaitFirstOrNull()
            ?: throw OpexError.CurrencyNotFound.exception()
        totalAssetsSnapshotRepository.createSnapshotsDirectly(currency.symbol, currency.precision.toInt())
            .awaitFirstOrNull()

        val end = System.currentTimeMillis()
        logger.info("Snapshot creation finished in {} ms", (end - start))
    }

    override suspend fun getUserLastSnapshot(
        uuid: String
    ): TotalAssetsSnapshot? {
        return totalAssetsSnapshotRepository.findLastSnapshotByUuid(uuid).awaitFirstOrNull()?.toTotalAssetsSnapshot()
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
