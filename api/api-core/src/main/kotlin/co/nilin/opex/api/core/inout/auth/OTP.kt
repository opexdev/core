package co.nilin.opex.api.core.inout.auth

import co.nilin.opex.api.core.inout.OTPType

data class OTPReceiver(
    val receiver: String,
    val type: OTPType,
)

data class OTPCode(
    val code: String,
    val otpType: OTPType,
)

data class OTPVerifyRequest(
    val userId: String,
    val otpCodes: List<OTPCode>
)

data class OTPVerifyResponse(
    val result: Boolean,
    val type: OTPResultType
)

data class TempOtpResponse(val otp: String?, val otpReceiver: OTPReceiver?)

enum class OTPAction {
    REGISTER, FORGET, NONE
}

enum class OTPResultType {
    VALID, EXPIRED, INCORRECT, INVALID
}