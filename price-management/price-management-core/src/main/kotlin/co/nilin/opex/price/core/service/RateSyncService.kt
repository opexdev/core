package co.nilin.opex.price.core.service

import co.nilin.opex.price.core.spi.WalletRateProxy
import org.slf4j.LoggerFactory
import java.math.BigDecimal

/**
 * Pushes a symbol's price to wallet's OTC rate table, but only when it actually changed —
 * every AUTO tick would otherwise call wallet even when nothing moved.
 */
class RateSyncService(
    private val walletRateProxy: WalletRateProxy
) {

    private val logger = LoggerFactory.getLogger(RateSyncService::class.java)

    suspend fun syncIfChanged(symbol: String, newPrice: BigDecimal, previousPrice: BigDecimal?) {
        if (previousPrice != null && previousPrice.compareTo(newPrice) == 0) return

        val parts = symbol.split("-", limit = 2)
        if (parts.size != 2) {
            logger.warn("Cannot sync rate to wallet: symbol=$symbol is not in SOURCE-DEST form")
            return
        }
        val (source, dest) = parts

        try {
            walletRateProxy.upsertRate(source, dest, newPrice)
            logger.info("Synced rate to wallet: $source-$dest = $newPrice")
        } catch (e: Exception) {
            logger.error("Failed to sync rate to wallet for symbol=$symbol: ${e.message}", e)
        }
    }
}
