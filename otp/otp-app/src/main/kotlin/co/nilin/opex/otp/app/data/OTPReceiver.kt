package co.nilin.opex.otp.app.data

import co.nilin.opex.otp.app.model.OTPType

data class OTPReceiver(
    val receiver: String,
    val type: OTPType,
)