package co.nilin.opex.wallet.ports.postgres.model

import co.nilin.opex.wallet.core.inout.otp.OTPType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("withdraws_otp")
data class WithdrawOtpModel(
    @Id var id: Long?,
    @Column("withdraw_uuid")
    val withdraw: String,
    val otpTracingCode: String,
    val otpType: OTPType,
    val createDate: LocalDateTime = LocalDateTime.now(),
)