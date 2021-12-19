package co.nilin.opex.wallet.app.dto

data class PaymentDepositRequest(
    val userId: String, // user uuid
    val amount: Double,
    val currency: PaymentCurrency,
    val reference: String,
    val description: String?
)