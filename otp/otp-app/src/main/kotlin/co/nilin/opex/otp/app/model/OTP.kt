package co.nilin.opex.otp.app.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("otp")
data class OTP(
    val code: String,
    val receiver: String,
    val userId: String,
    val action: String,
    val tracingCode: String,
    val type: OTPType,
    val expiresAt: LocalDateTime,
    val requestDate: LocalDateTime = LocalDateTime.now(),
    val isVerified: Boolean = false,
    val isActive: Boolean = true,
    val verifyTime: LocalDateTime? = null,
    @Id val id: Long? = null,
) {

    fun isExpired(): Boolean {
        return expiresAt.isBefore(LocalDateTime.now())
    }
}