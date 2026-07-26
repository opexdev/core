package co.nilin.opex.otp.app.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("totp")
data class TOTP(
    val userId: String,
    val secret: String,
    val label: String? = null,
    var isEnabled: Boolean = true,
    var isActivated: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Id val id: Long? = null
)