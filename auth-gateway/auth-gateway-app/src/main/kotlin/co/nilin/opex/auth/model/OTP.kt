package co.nilin.opex.auth.model

import jakarta.validation.constraints.NotBlank

data class OTPReceiver(
    val receiver: String,
    val type: OTPType,
)

data class OTPCode(
    @field:NotBlank(message = "code is required")
    val code: String,

    @field:NotBlank(message = "otpType is required")
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

//TODO IMPORTANT: remove in production
data class TempOtpResponse(val otp: String, val otpReceiver: OTPReceiver?)

enum class OTPAction {
    REGISTER, FORGET, NONE
}

enum class OTPResultType {
    VALID, EXPIRED, INCORRECT, INVALID
}