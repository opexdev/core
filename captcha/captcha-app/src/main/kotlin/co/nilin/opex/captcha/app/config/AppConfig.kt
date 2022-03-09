package co.nilin.opex.captcha.app.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.ConcurrentLruCache

@Configuration
class AppConfig {
    @Bean
    fun store(): ConcurrentLruCache<String, Boolean> = ConcurrentLruCache(Int.MAX_VALUE / 64) { true }
}
