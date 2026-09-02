package co.nilin.opex.price.core.spi

import java.math.BigDecimal

interface WalletRateProxy {
    suspend fun upsertRate(sourceSymbol: String, destSymbol: String, rate: BigDecimal)
}
