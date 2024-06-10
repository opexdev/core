package co.nilin.opex.market.app.config

import co.nilin.opex.market.ports.postgres.util.CacheHelper
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.cache.RedisCacheManager.RedisCacheManagerBuilder
import org.springframework.data.redis.cache.RedisCacheWriter
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    fun cacheManager(connectionFactory: RedisConnectionFactory, mapper: ObjectMapper): CacheManager {
        val newMapper = mapper.copy().apply {
            activateDefaultTyping(mapper.polymorphicTypeValidator, ObjectMapper.DefaultTyping.NON_FINAL)
            findAndRegisterModules()
            registerKotlinModule()
        }
        return RedisCacheManagerBuilder.fromConnectionFactory(connectionFactory)
            .withCacheConfiguration(
                "marketCache",
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(10))
                    .disableCachingNullValues()
                    .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                            .fromSerializer(GenericJackson2JsonRedisSerializer(newMapper))
                    )
            )
            .build()
    }

    @Bean
    fun marketCacheWrapper(cacheManager: CacheManager): CacheHelper {
        return CacheHelper(cacheManager, "marketCache")
    }
}