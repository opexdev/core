package co.nilin.opex.api.core.inout

data class DepositWebhookResponse(
    val referenceNumber: String? = null,
    val status: String,
    val duplicate: Boolean = false,
    val message: String? = null
)

