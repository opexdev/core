package co.nilin.opex.api.app.config

import co.nilin.opex.api.ports.postgres.dao.SymbolMapRepository
import co.nilin.opex.utility.preferences.ProjectPreferences
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component
import java.io.File

@Component
@DependsOn("postgresConfig")
class SetupPreferences(
    @Value("\${app.preferences}") file: File,
    symbolMapRepository: SymbolMapRepository
) {
    private val mapper = ObjectMapper(YAMLFactory())

    init {
        val p: ProjectPreferences = mapper.readValue(file, ProjectPreferences::class.java)
        runBlocking {
            p.markets.map {
                launch {
                    val pair = it.pair ?: "${it.leftSide}_${it.rightSide}"
                    symbolMapRepository.insert(pair, it.aliases.first()).awaitSingleOrNull()
                }
            }
        }
    }
}
