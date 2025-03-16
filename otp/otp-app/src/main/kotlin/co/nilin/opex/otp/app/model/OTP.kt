package co.nilin.opex.otp.app.model

import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("otp")
data class OTP(
    val code: Int,
    val subject: String,
    val tracingCode: String,
    val type: OTPType,
    val expiresAt: LocalDateTime,
    val id: Long? = null,
)