package co.nilin.opex.auth.model

import jakarta.validation.constraints.NotBlank

data class NewOTPRequest(
    val userId: String,
    val receivers: List<OTPReceiver>
)

data class OTPReceiver(
    val receiver: String,
    val type: OTPType,
)

data class OTPSendResponse(
    val tracingCode: String
)

data class OTPCode(
    @field:NotBlank(message = "code is required")
    val code: String,

    @field:NotBlank(message = "otpType is required")
    val otpType: OTPType,
)

data class OTPVerifyRequest(
    @field:NotBlank(message = "tracingCode is required")
    val tracingCode: String,
    val otpCodes: List<OTPCode>
)

data class OTPVerifyResponse(
    val result: Boolean
)