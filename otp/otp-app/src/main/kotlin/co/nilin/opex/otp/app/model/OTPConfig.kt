package co.nilin.opex.otp.app.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("otp_config")
data class OTPConfig(
    @Id val type: OTPType,
    var expireTimeSeconds: Int = 60,
    var charCount: Int = 6,
    var includeAlphabetChars: Boolean = false,
    var isEnabled: Boolean = true,
    var isActivated: Boolean = false,
    var messageTemplate: String = "%s",
)