package co.nilin.opex.otp.app.data

import javax.validation.constraints.NotBlank

data class VerifyOTPRequest(
    @field:NotBlank(message = "code is required")
    val code:String,
    @field:NotBlank(message = "tracingCode is required")
    val tracingCode:String,
)
