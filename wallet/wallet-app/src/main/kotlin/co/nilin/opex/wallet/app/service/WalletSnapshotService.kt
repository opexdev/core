package co.nilin.opex.wallet.app.service

import co.nilin.opex.wallet.app.service.otc.GraphService
import co.nilin.opex.wallet.core.spi.TotalAssetsSnapshotManager
import co.nilin.opex.wallet.ports.postgres.dao.PriceRepository
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class WalletSnapshotService(
    private val totalAssetsSnapshotManager: TotalAssetsSnapshotManager,
    private val graphService: GraphService,
    private val priceRepository: PriceRepository,
    @Value("\${app.snapshot-currency}")
    private val snapshotCurrency: String
) {

    @Scheduled(cron = "0 0 0 * * ?", zone = "GMT" + "\${app.zone-offset}")
    fun createSnapshots() {
        runBlocking {
            updatePrices()
            totalAssetsSnapshotManager.createSnapshot()
        }
    }

    private suspend fun updatePrices() {
        val currencyPrices = graphService.fetchPrice(snapshotCurrency)
        currencyPrices?.forEach { currencyPrice ->
            priceRepository.upsert(
                currencyPrice.currency, snapshotCurrency, currencyPrice.sellPrice ?: BigDecimal.ZERO
            ).awaitFirstOrNull()
        }
    }
}
