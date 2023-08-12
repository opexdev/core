package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.spi.JsonMapper
import com.google.gson.Gson
import org.springframework.boot.json.GsonJsonParser

class JsonMapperTestImpl : JsonMapper {

    override fun serialize(input: Any): String? {
        return Gson().toJson(input)
    }

    override fun <T> deserialize(input: String, t: Class<T>): T {
        return Gson().fromJson(input, t)
    }

    override fun toMap(input: Any): Map<String, Any> {
        return GsonJsonParser().parseMap(serialize(input))
    }
}