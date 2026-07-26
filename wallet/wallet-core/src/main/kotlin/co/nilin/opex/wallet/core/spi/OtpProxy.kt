package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.otp.NewOTPRequest
import co.nilin.opex.wallet.core.inout.otp.OTPVerifyResponse
import co.nilin.opex.wallet.core.inout.otp.TempOtpResponse
import co.nilin.opex.wallet.core.inout.otp.VerifyOTPRequest


interface OtpProxy {
    suspend fun requestOTP(newOTPRequest: NewOTPRequest): TempOtpResponse
    suspend fun verifyOTP(verifyOTPRequest: VerifyOTPRequest): OTPVerifyResponse
}