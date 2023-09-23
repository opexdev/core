package co.nilin.opex.config.ports.redis.config

import co.nilin.opex.config.core.model.GeneralSystemConfig
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.redis.om.spring.annotations.EnableRedisDocumentRepositories
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
@EnableRedisDocumentRepositories(basePackages = ["co.nilin.opex.config.ports.redis.*"])
class RedisConfig(
   /* @Value("\${spring.redis.host}")
    private val redisHost: String,
    @Value("\${spring.redis.port}")
    private val redisPort: String,*/
) {

    /*@Bean("generalSystemConfigTemplate")
    fun systemConfigRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, GeneralSystemConfig> {
        val jackson2JsonRedisSerializer = Jackson2JsonRedisSerializer(GeneralSystemConfig::class.java)
        val objectMapper = ObjectMapper().apply { setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY) }
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper)

        val serializationContext =
            RedisSerializationContext.newSerializationContext<String, GeneralSystemConfig>(StringRedisSerializer())
                .hashKey(StringRedisSerializer())
                .hashValue(jackson2JsonRedisSerializer)
                .build()
        return ReactiveRedisTemplate(factory, serializationContext)
    }*/

    /*@Bean
    fun redisCommands():RedisModulesCommands<String,String> {
        val client=RedisModulesClient.create("redis://$redisHost:$redisPort")
        val connection=client.connect()
        return connection.sync()
    }*/

}