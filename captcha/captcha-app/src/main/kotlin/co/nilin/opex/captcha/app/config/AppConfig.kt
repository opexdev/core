package co.nilin.opex.captcha.app.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.ConcurrentHashMap

@Configuration
class AppConfig {
    // TODO: It must be replaced with a concurrent LRU cache. Captcha session must expire after usage
    @Bean
    fun store(): MutableMap<String, String> = ConcurrentHashMap()
}
