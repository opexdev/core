package co.nilin.opex.api.core.inout

data class TempOtpResponse(val otp: String, var receivers: List<OTPReceiver>? = null)