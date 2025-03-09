package co.nilin.opex.auth.model

import jakarta.validation.constraints.NotBlank

data class OTPSendRequest(
    @field:NotBlank(message = "receiver is required")
    val receiver: String,
    @field:NotBlank(message = "type is required")
    val type: String, //valid otp types can be fetched from server
)

data class OTPSendResponse(
    val tracingCode: String
)

data class OTPVerifyRequest(
    @field:NotBlank(message = "tracingCode is required")
    val tracingCode: String,
    @field:NotBlank(message = "code is required")
    val code: String
)