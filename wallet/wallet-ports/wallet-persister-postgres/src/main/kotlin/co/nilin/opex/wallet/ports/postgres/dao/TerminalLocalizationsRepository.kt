package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.TerminalLocalizationModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface TerminalLocalizationsRepository : ReactiveCrudRepository<TerminalLocalizationModel, Long> {
    suspend fun findByTerminalId(terminalId: Long): Flux<TerminalLocalizationModel>
}