package co.nilin.opex.api.core.inout

data class OTPReceiver(
    val receiver: String,
    val type: OTPType,
)