package co.nilin.opex.matching.gateway.app.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.cache.annotation.EnableCaching
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
            // DefaultTyping.EVERYTHING is not a very good way. Alternatives:
            // 1. Wrap all values inside a java class (CacheValueWrapper)
            // 2. Use DefaultTyping.JAVA_LANG_OBJECT instead and add
            //    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS) on all kt classes
            //    https://github.com/FasterXML/jackson-databind/issues/4427
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
}