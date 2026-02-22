package co.nilin.opex.api.ports.opex.util

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.util.*

class DateToEpochSerializer :
    JsonSerializer<Date>() {

    override fun serialize(
        value: Date?,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        if (value == null) {
            gen.writeNull()
            return
        }
        gen.writeNumber(value.time)
    }
}