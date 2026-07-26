package co.nilin.opex.profile.core.spi

import co.nilin.opex.profile.core.data.otp.NewOTPRequest
import co.nilin.opex.profile.core.data.otp.OTPVerifyResponse
import co.nilin.opex.profile.core.data.otp.TempOtpResponse
import co.nilin.opex.profile.core.data.otp.VerifyOTPRequest

interface OtpProxy {
    suspend fun requestOtp(newOTPRequest: NewOTPRequest) : TempOtpResponse
    suspend fun verifyOtp(verifyOTPRequest: VerifyOTPRequest) : OTPVerifyResponse
}