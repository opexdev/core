package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class UserTotalVolumeValue(
    val valueUSDT: BigDecimal,
    val valueIRT: BigDecimal
)