package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class SubmitVoucherResponse(
    val amount: BigDecimal,
    val currency: String,
    var issuer: String?,
    var description: String? = null
)
