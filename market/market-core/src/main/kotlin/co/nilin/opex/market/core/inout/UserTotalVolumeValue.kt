package co.nilin.opex.market.core.inout

import java.math.BigDecimal

data class UserTotalVolumeValue(
    val valueUSDT: BigDecimal,
    val valueIRT: BigDecimal
) {
    companion object {
        fun zero() = UserTotalVolumeValue(BigDecimal.ZERO, BigDecimal.ZERO)
    }
}