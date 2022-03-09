package co.nilin.opex.captcha.app.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.ConcurrentHashMap

@Configuration
class AppConfig {
    @Bean
    fun store(): ConcurrentHashMap<String, Long> = ConcurrentHashMap(1024)
}
