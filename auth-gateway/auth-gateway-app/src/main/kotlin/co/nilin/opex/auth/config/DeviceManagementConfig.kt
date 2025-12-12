package co.nilin.opex.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app.device-management")
class DeviceManagementConfig {
    lateinit var url: String
}