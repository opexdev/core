package co.nilin.opex.accountant.app.config

import co.nilin.opex.accountant.core.spi.JsonMapper
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JsonMapperConfig {

    @Bean
    fun getJsonMapper(objectMapper: ObjectMapper): JsonMapper {
        return object : JsonMapper {
            override fun serialize(input: Any): String? {
                return objectMapper.writeValueAsString(input)
            }

            override fun <T> deserialize(input: String, t: Class<T>): T {
                return objectMapper.readValue(input, t)
            }

            override fun toMap(input: Any): Map<String, Any> {
                return objectMapper.convertValue(input, Map::class.java) as Map<String, Any>
            }
        }
    }

}