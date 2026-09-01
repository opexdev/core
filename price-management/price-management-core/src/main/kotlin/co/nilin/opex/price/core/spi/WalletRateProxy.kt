package co.nilin.opex.price.core.spi

import java.math.BigDecimal

interface WalletRateProxy {
    /** Creates the rate pair in wallet if it doesn't exist yet, otherwise updates it. */
    suspend fun upsertRate(sourceSymbol: String, destSymbol: String, rate: BigDecimal)
}
