package co.nilin.opex.wallet.core.inout.otp

data class OTPVerifyResponse(
    val result: Boolean,
    val type: OTPResultType,
    val tracingCode: String? = null,
)
