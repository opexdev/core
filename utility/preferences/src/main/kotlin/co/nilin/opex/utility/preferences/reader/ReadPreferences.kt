package co.nilin.opex.utility.preferences.reader

import co.nilin.opex.utility.preferences.ProjectPreferences
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File

@Configuration
class ReadPreferences() {
    private val mapper = ObjectMapper(YAMLFactory())

    @Value("\${app.preferences}")
    private lateinit var preferencesYmlPath: String

    @Bean
    fun preferences(): ProjectPreferences {
        val preferencesYml = File(preferencesYmlPath)
        return mapper.readValue(preferencesYml, ProjectPreferences::class.java)
    }
}
