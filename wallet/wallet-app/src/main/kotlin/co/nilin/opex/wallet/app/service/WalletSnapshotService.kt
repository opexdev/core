package co.nilin.opex.wallet.app.service

import co.nilin.opex.wallet.app.service.otc.GraphService
import co.nilin.opex.wallet.core.spi.UserAssetsSnapshotManager
import co.nilin.opex.wallet.ports.postgres.dao.PriceRepository
import co.nilin.opex.wallet.ports.postgres.util.RedisCacheHelper
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class WalletSnapshotService(
    private val userAssetsSnapshotManager: UserAssetsSnapshotManager,
    private val graphService: GraphService,
    private val priceRepository: PriceRepository,
    private val redisCacheHelper: RedisCacheHelper,
    @Value("\${app.snapshot-currency}")
    private val snapshotCurrency: String
) {

    @Scheduled(cron = "0 0 0 * * ?", zone = "GMT" + "\${app.zone-offset}")
    fun createTotalAssetsSnapshot() {
        runBlocking {
            updatePrices()
            userAssetsSnapshotManager.createTotalAssetsSnapshot()
        }
    }

    @Scheduled(cron = "0 40 16 * * ?", zone = "GMT" + "\${app.zone-offset}")
    fun createDetailAssetsSnapshot() {
        runBlocking {
            updatePrices()
            userAssetsSnapshotManager.createDetailAssetsSnapshot()
            redisCacheHelper.evictWithPrefix("users-detail-assets:")
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
