package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.spi.JsonMapper
import com.fasterxml.jackson.databind.ObjectMapper

class JsonMapperTestImpl : JsonMapper {

    private val objectMapper = ObjectMapper().apply {
        findAndRegisterModules()
    }

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