package co.nilin.opex.wallet.core.inout.otp

data class TempOtpResponse(val otp: String?, var otpReceiver: OTPReceiver? = null)
