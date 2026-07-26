package co.nilin.opex.api.ports.opex.util

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.time.LocalDate
import java.time.ZoneOffset

class LocalDateToEpochSerializer : JsonSerializer<LocalDate>() {
    override fun serialize(
        value: LocalDate?,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        if (value == null) {
            gen.writeNull()
            return
        }

        val epochMillis = value
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()

        gen.writeNumber(epochMillis)
    }
}