package co.nilin.opex.otp.app.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("TOTP")
data class TOTPConfig(
    var secretChars: Int,
    var issuer: String,
    @Id val id: Boolean = true
)