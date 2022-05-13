package co.nilin.opex.matching.engine.app.config

import co.nilin.opex.utility.preferences.ProjectPreferences
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File

@Configuration
class SetupPreferences(@Value("\${app.preferences}") private val file: File) {
    @Bean("symbols")
    fun getSymbols(): List<String> {
        val mapper = ObjectMapper(YAMLFactory())
        val p: ProjectPreferences = mapper.readValue(file, ProjectPreferences::class.java)
        return p.markets.map { it.pair ?: "${it.leftSide}_${it.rightSide}" }.map { it.lowercase() }
    }
}
