package co.nilin.opex.referral.core.spi

import java.math.BigDecimal

interface AccountantProxy {
    suspend fun fetchLastPrice(pairSymbol: String): BigDecimal?
}
