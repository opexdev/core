package co.nilin.opex.price.app.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class CacheConfig {

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory, mapper: ObjectMapper): RedisTemplate<String, Any> {
        val cacheMapper = mapper.copy().apply {
            activateDefaultTyping(polymorphicTypeValidator, ObjectMapper.DefaultTyping.EVERYTHING)
            findAndRegisterModules()
            registerKotlinModule()
        }
        return RedisTemplate<String, Any>().apply {
            setConnectionFactory(connectionFactory)
            val serializer = GenericJackson2JsonRedisSerializer(cacheMapper)
            valueSerializer = serializer
            hashValueSerializer = serializer
            keySerializer = StringRedisSerializer()
            hashKeySerializer = StringRedisSerializer()
            afterPropertiesSet()
        }
    }
}
