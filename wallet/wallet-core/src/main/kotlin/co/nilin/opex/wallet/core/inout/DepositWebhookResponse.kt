package co.nilin.opex.wallet.core.inout

data class DepositWebhookResponse(
    val referenceNumber: String,
    val status: String,
    val duplicate: Boolean = false,
    val message: String? = null
)

