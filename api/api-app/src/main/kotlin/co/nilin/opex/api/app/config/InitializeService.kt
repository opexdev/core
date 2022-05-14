package co.nilin.opex.api.app.config

import co.nilin.opex.api.ports.postgres.dao.SymbolMapRepository
import co.nilin.opex.api.ports.postgres.model.SymbolMapModel
import co.nilin.opex.utility.preferences.Preferences
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
@DependsOn("postgresConfig")
class InitializeService(private val symbolMapRepository: SymbolMapRepository) {
    @Autowired
    private lateinit var preferences: Preferences

    @PostConstruct
    fun init() = runBlocking {
        preferences.markets.map {
            val pair = it.pair ?: "${it.leftSide}_${it.rightSide}"
            val items = it.aliases.map { a -> SymbolMapModel(null, pair, a.key, a.alias) }
            runCatching { symbolMapRepository.saveAll(items).collectList().awaitSingleOrNull() }
        }
    }
}
