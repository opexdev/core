package co.nilin.opex.otp.app.data

import co.nilin.opex.otp.app.model.OTPType
import javax.validation.constraints.NotBlank

data class NewOTPRequest(
    @field:NotBlank(message = "receiver is required")
    val receiver: String,
    @field:NotBlank(message = "type is required")
    val type: OTPType,
)