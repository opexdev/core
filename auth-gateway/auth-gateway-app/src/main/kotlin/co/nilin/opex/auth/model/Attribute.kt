package co.nilin.opex.auth.model

data class Attribute(
    val key: String,
    val value: String
)

object Attributes {

    const val EMAIL = "email"
    const val MOBILE = "mobile"
    const val OTP = "otpConfig"
}