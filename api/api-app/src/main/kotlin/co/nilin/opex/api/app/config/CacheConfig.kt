package co.nilin.opex.api.app.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory, mapper: ObjectMapper): RedisTemplate<String, Any> {
        val newMapper = mapper.copy().apply {
            activateDefaultTyping(mapper.polymorphicTypeValidator, ObjectMapper.DefaultTyping.EVERYTHING)
            findAndRegisterModules()
            registerKotlinModule()
        }
        return RedisTemplate<String, Any>().apply {
            setConnectionFactory(connectionFactory)
            val ser = GenericJackson2JsonRedisSerializer(newMapper)
            valueSerializer = ser
            hashValueSerializer = ser
            keySerializer = StringRedisSerializer()
            hashKeySerializer = StringRedisSerializer()
            afterPropertiesSet()
        }
    }

    @Bean
    fun apiKeyCacheManager(): CacheManager {
        return ConcurrentMapCacheManager("apiKey")
    }

}