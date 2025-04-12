package co.nilin.opex.otp.app.model

enum class OTPType(val compositeOrder: Int) {

    SMS(0), EMAIL(1),
    COMPOSITE(99)
}