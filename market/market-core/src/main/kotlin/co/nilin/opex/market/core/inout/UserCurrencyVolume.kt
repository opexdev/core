package co.nilin.opex.market.core.inout

import java.math.BigDecimal

data class UserCurrencyVolume(
    val currency: String,
    val volume: BigDecimal,
    val valueUSDT: BigDecimal,
    val valueIRT: BigDecimal
) {
    companion object {
        fun zero(currency: String) = UserCurrencyVolume(currency, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
    }
}