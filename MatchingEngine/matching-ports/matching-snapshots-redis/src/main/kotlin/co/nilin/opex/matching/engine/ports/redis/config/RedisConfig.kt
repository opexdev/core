package co.nilin.opex.matching.engine.ports.redis.config

import co.nilin.opex.matching.engine.core.model.PersistentOrderBook
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig() {
    @Bean("snapshotRedisTemplate")
    fun snapshotRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, PersistentOrderBook> {
        val jackson2JsonRedisSerializer = Jackson2JsonRedisSerializer(PersistentOrderBook::class.java)
        val objectMapper = ObjectMapper()
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper)
        val serializationContext =
            RedisSerializationContext.newSerializationContext<String, PersistentOrderBook>(StringRedisSerializer())
                .hashKey(StringRedisSerializer())
                .hashValue(jackson2JsonRedisSerializer)
                .build()

        return ReactiveRedisTemplate(factory, serializationContext)
    }
}