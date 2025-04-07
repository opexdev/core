package co.nilin.opex.otp.app.model

import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("otp")
data class OTP(
    val code: String,
    val receiver: String,
    val tracingCode: String,
    val type: OTPType,
    val expiresAt: LocalDateTime,
    val id: Long? = null,
) {

    fun isExpired(): Boolean {
        return LocalDateTime.now().isAfter(expiresAt)
    }
}