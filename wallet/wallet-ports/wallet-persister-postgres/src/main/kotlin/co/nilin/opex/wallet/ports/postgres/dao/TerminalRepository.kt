package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.dto.TerminalView
import co.nilin.opex.wallet.ports.postgres.model.TerminalModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TerminalRepository : ReactiveCrudRepository<TerminalModel, Long> {
    fun findByIdentifier(identifier: String): Mono<TerminalModel>?

    @Query(
        """
    SELECT t.id,
       t.uuid,
       t.owner,
       t.identifier,
       t.active,
       t.type,
       t.meta_data,
       t.display_order,
       tl.description
    FROM terminal t
    LEFT JOIN terminal_localization tl 
      ON tl.terminal_id = t.id 
      AND tl.language = :lang
    WHERE t.uuid = :uuid;
    """
    )
    fun findByUuid(uuid: String, lang: String? = null): Mono<TerminalView>?

    @Query(
        """
    SELECT t.id,
       t.uuid,
       t.owner,
       t.identifier,
       t.active,
       t.type,
       t.meta_data,
       t.display_order,
       tl.description
    FROM terminal t
    LEFT JOIN terminal_localization tl 
      ON tl.terminal_id = t.id 
      AND tl.language = :lang
    order by t.display_order
    """
    )
    fun findAllByOrderByDisplayOrder(lang: String?= null): Flux<TerminalView>


}