package co.nilin.opex.api.app.config

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    fun apiKeyCacheManager(): CacheManager {
        return ConcurrentMapCacheManager("apiKey")
    }

}