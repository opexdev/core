package co.nilin.opex.api.core.inout

import com.fasterxml.jackson.annotation.JsonInclude

data class TwoFactorRequest(
    val method: OTPType,
)

data class ConfirmTwoFactorRequest(
    val method: OTPType,
    val otp: String,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TwoFactorResponse(val otp: String?, val otpReceiver: OTPReceiver?)
