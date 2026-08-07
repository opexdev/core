package co.nilin.opex.api.core.inout.auth

data class Attribute(
    val key: String,
    val value: String
)

object Attributes {

    const val EMAIL = "email"
    const val MOBILE = "mobile"
    const val OTP = "otpConfig"
}