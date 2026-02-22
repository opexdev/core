package co.nilin.opex.api.ports.opex.config

import co.nilin.opex.api.ports.opex.util.DateToEpochSerializer
import co.nilin.opex.api.ports.opex.util.LocalDateTimeToEpochSerializer
import co.nilin.opex.api.ports.opex.util.LocalDateToEpochSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Configuration
class ApiJacksonConfig {

    @Bean
    @Primary
    fun apiObjectMapper(): ObjectMapper {

        val module = SimpleModule().apply {
            addSerializer(LocalDateTime::class.java, LocalDateTimeToEpochSerializer())
            addSerializer(LocalDate::class.java, LocalDateToEpochSerializer())
            addSerializer(Date::class.java, DateToEpochSerializer())

        }

        return JsonMapper.builder()
            .addModule(JavaTimeModule())
            .addModule(KotlinModule.Builder().build())
            .addModule(module)
            .build()
    }
}