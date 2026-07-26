package co.nilin.opex.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app.captcha")
class CaptchaConfig {
    lateinit var url: String
}