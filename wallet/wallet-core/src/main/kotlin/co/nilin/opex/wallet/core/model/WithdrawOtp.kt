package co.nilin.opex.wallet.core.model

import co.nilin.opex.wallet.core.inout.otp.OTPType
import java.time.LocalDateTime

data class WithdrawOtp(
    val withdraw: String,
    val otpTracingCode: String,
    val otpType: OTPType,
    val createDate: LocalDateTime = LocalDateTime.now(),
)