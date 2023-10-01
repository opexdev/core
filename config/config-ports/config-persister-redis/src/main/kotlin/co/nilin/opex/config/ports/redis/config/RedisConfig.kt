package co.nilin.opex.config.ports.redis.config

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate

@Configuration
@EnableRedisDocumentRepositories(basePackages = ["co.nilin.opex.config.ports.redis.*"])
class RedisConfig {

    @Bean("systemKeyValueConfigRedisTemplate")
    fun systemConfigRedisTemplate(factory: RedisConnectionFactory): RedisTemplate<String, String> {
        return RedisTemplate<String, String>().apply {
            setConnectionFactory(factory)
        }
    }

}