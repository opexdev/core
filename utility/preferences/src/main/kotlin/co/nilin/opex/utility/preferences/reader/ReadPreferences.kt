package co.nilin.opex.utility.preferences.reader

import co.nilin.opex.utility.preferences.Preferences
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File

@Configuration
class ReadPreferences(
    @Value("\${PREFERENCES:/preferences.yml}")
    private val preferencesYmlPath: String
) {
    private val mapper = ObjectMapper(YAMLFactory())

    @Bean
    fun preferences(): Preferences = runCatching {
        if (preferencesYmlPath.isBlank()) return Preferences()
        if (File(preferencesYmlPath).exists()) {
            return@runCatching mapper.readValue(File(preferencesYmlPath), Preferences::class.java)
        } else {
            val ymlInputStream = javaClass.getResourceAsStream(preferencesYmlPath)
            if (ymlInputStream != null)
                return@runCatching mapper.readValue(ymlInputStream, Preferences::class.java)
            Preferences()
        }
    }.getOrElse { throw IllegalStateException("Failed to load preferences: ${it.message}") }
}
