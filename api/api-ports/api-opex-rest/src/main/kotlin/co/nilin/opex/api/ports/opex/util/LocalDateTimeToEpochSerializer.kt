package co.nilin.opex.api.ports.opex.util

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.time.LocalDateTime
import java.time.ZoneOffset

class LocalDateTimeToEpochSerializer :
    JsonSerializer<LocalDateTime>() {

    override fun serialize(
        value: LocalDateTime,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        val epochMillis = value
            .atOffset(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()

        gen.writeNumber(epochMillis)
    }
}
