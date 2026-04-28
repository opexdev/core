package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.TerminalLocalizationModel
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TerminalLocalizationsRepository : ReactiveCrudRepository<TerminalLocalizationModel, Long> {
    suspend fun findByTerminalId(terminalId: Long): Flux<TerminalLocalizationModel>

    @Modifying
    @Query(
        """
    INSERT INTO terminal_localization (terminal_id, description, owner, language)
    VALUES (:terminalId, :description, :owner, :language)
    ON CONFLICT (terminal_id, language)
    DO UPDATE SET
        description = :description,
        owner = :owner
"""
    )
    fun upsert(
        terminalId: Long,
        description: String?,
        owner: String?,
        language: String
    ): Mono<Void>
}