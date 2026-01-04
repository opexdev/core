package co.nilin.opex.wallet.app.dto

import co.nilin.opex.wallet.core.inout.TransferMethod
import java.math.BigDecimal

data class PaymentDepositRequest(
    val userId: String, // user uuid
    val amount: BigDecimal,
    val currency: String,
    val reference: String,
    val description: String?,
    val isIPG: Boolean? = true,
    val transferMethod: TransferMethod? = null
)