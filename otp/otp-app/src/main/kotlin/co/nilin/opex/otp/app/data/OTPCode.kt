package co.nilin.opex.otp.app.data

import co.nilin.opex.otp.app.model.OTPType

data class OTPCode(
    val type: OTPType,
    val code: String
)
