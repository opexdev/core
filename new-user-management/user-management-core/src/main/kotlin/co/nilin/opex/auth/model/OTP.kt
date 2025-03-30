package co.nilin.opex.auth.model

import jakarta.validation.constraints.NotBlank

data class OTPSendRequest(
    @field:NotBlank(message = "receiver is required")
    val receiver: String,
    @field:NotBlank(message = "type is required")
    val otpTypes: List<String>
)

data class OTPSendResponse(
    val tracingCode: String,
    val otpTypes: List<String>
)

data class OTPCode (
    @field:NotBlank(message = "code is required")
    val code: String,

    @field:NotBlank(message = "otpType is required")
    val otpType: String,
)


data class OTPVerifyRequest(
    @field:NotBlank(message = "tracingCode is required")
    val tracingCode: String,
    val otpCodes: List<OTPCode>
)