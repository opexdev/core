package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.spi.MarketProxy
import co.nilin.opex.wallet.core.spi.TotalAssetsSnapshotManager
import co.nilin.opex.wallet.ports.postgres.dao.TotalAssetsSnapshotRepository
import co.nilin.opex.wallet.ports.postgres.dao.WalletOwnerRepository
import co.nilin.opex.wallet.ports.postgres.dao.WalletRepository
import co.nilin.opex.wallet.ports.postgres.model.TotalAssetsSnapshotModel
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class TotalAssetsSnapshotImpl(
    private val totalAssetsSnapshotRepository: TotalAssetsSnapshotRepository,
    private val walletRepository: WalletRepository,
    private val walletOwnerRepository: WalletOwnerRepository,
    private val marketProxy: MarketProxy
) : TotalAssetsSnapshotManager {

    private val logger = LoggerFactory.getLogger(TotalAssetsSnapshotImpl::class.java)

    override suspend fun createSnapshotForAllOwners() {
        val irtPrice = getIrtPrice()
        val prices = getPrices()

        val allOwners = walletOwnerRepository.findAll().collectList().awaitFirstOrNull() ?: return

        allOwners.forEach { owner ->
            val wallets = walletRepository.findNonZeroByOwnerExcludeCashout(owner.id!!).collectList().awaitFirstOrNull()
                ?: emptyList()

            val totalUsdt = wallets.sumOf { wallet ->
                wallet.balance * (prices[wallet.currency] ?: BigDecimal.ZERO)
            }

            if (totalUsdt > BigDecimal.ZERO) {
                val totalIrt = totalUsdt * irtPrice
                val snapshot = TotalAssetsSnapshotModel(
                    null, owner.id!!, totalIrt, totalUsdt, LocalDateTime.now()
                )
                totalAssetsSnapshotRepository.save(snapshot).awaitFirst()
            }
        }
    }

    private suspend fun getPrices(): Map<String, BigDecimal> {
        val prices = marketProxy.fetchPrices()
        return prices.filter { it.symbol.endsWith("_USDT") }.associate {
            val currency = it.symbol.substringBefore("_USDT")
            currency to it.price.toBigDecimal()
        }
    }

    private suspend fun getIrtPrice(): BigDecimal {
        val prices = marketProxy.fetchPrices("USDT_IRT")
        return if (prices.isNotEmpty()) prices[0].price.toBigDecimal() else BigDecimal.ZERO
    }
}
