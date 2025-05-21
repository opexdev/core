package co.nilin.opex.otp.app.data

data class OTPResult(val result: Boolean, val type: OTPResultType)

enum class OTPResultType(val isValid: Boolean = false) {

    VALID(true), EXPIRED, INCORRECT, INVALID
}