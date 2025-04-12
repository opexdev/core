package co.nilin.opex.otp.app.proxy.data

data class KaveSendResponse(
    val messageId: Long,
    val message: String?,
    val status: Int,
    val statusText: String?,
    val sender: String?,
    val receptor: String?,
    val cost: Long
)