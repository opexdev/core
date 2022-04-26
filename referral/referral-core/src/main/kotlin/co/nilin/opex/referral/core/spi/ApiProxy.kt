package co.nilin.opex.referral.core.spi

import java.math.BigDecimal

interface ApiProxy {
    suspend fun fetchLastPrice(pairSymbol: String): BigDecimal?
}
