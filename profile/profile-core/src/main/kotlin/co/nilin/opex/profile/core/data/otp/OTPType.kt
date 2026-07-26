package co.nilin.opex.profile.core.data.otp

enum class OTPType(val compositeOrder: Int) {

    SMS(0), EMAIL(1),
    COMPOSITE(99)
}