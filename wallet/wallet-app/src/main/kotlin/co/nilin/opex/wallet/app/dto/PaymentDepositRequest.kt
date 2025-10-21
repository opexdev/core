package co.nilin.opex.wallet.app.dto

import java.math.BigDecimal

data class PaymentDepositRequest(
    val userId: String, // user uuid
    val amount: BigDecimal,
    val currency: PaymentCurrency,
    val reference: String,
    val description: String?,
    val isIPG: Boolean? = true
)