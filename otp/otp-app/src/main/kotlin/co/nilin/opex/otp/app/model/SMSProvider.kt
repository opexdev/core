package co.nilin.opex.otp.app.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table


@Table(name = "sms_provider")
class SMSProvider(
    @Id
    val id: String,
    val enabled: Boolean,
    val baseUrl: String,
    val apiKey: String?,
    val template: String?,
    val username: String?,
    val password: String?,
    val sender: String?,
    val extraConfig: String?
)