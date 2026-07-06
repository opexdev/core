package co.nilin.opex.otp.app.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table(name = "sms_provider_route")
class SMSProviderRoute(
    @Id
    val id: Long? = null,
    val prefix: String,
    val provider: String,
    val enabled: Boolean = true
)