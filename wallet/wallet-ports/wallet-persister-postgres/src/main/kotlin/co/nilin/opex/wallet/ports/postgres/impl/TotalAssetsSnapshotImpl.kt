package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.model.TotalAssetsSnapshot
import co.nilin.opex.wallet.core.spi.MarketProxy
import co.nilin.opex.wallet.core.spi.TotalAssetsSnapshotManager
import co.nilin.opex.wallet.ports.postgres.dao.PriceRepository
import co.nilin.opex.wallet.ports.postgres.dao.TotalAssetsSnapshotRepository
import co.nilin.opex.wallet.ports.postgres.util.toTotalAssetsSnapshot
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TotalAssetsSnapshotImpl(
    private val totalAssetsSnapshotRepository: TotalAssetsSnapshotRepository,
    private val priceRepository: PriceRepository,
    private val marketProxy: MarketProxy
) : TotalAssetsSnapshotManager {

    private val logger = LoggerFactory.getLogger(TotalAssetsSnapshotImpl::class.java)

    override suspend fun createSnapshot() {
        val start = System.currentTimeMillis()
        logger.info("Starting snapshot creation...")

        savePrices()
        totalAssetsSnapshotRepository.createSnapshotsDirectly().awaitFirstOrNull()

        val end = System.currentTimeMillis()
        logger.info("Snapshot creation finished in {} ms", (end - start))
    }

    override suspend fun getByOwnerIdAndDate(
        ownerId: Long, fromDate: LocalDateTime?, toDate: LocalDateTime?
    ): List<TotalAssetsSnapshot> {
        return totalAssetsSnapshotRepository.findByOwnerIdAndSnapshotDate(ownerId, fromDate, toDate).collectList()
            .awaitFirstOrNull()?.map { it.toTotalAssetsSnapshot() } ?: emptyList()
    }

    suspend fun savePrices() {
        val fetched = marketProxy.fetchPrices()
        fetched.forEach { ticker ->
            priceRepository.upsert(
                ticker.symbol, ticker.price.toBigDecimal()
            ).awaitFirstOrNull()
        }
    }

}
