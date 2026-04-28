package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.dto.TerminalView
import co.nilin.opex.wallet.ports.postgres.model.GatewayTerminalModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface GatewayTerminalRepository : ReactiveCrudRepository<GatewayTerminalModel, Long> {

    fun deleteByTerminalIdAndGatewayId(terminalId: Long, gatewayId: Long): Mono<Void>

    @Query(
        """
    SELECT t.id,
       t.uuid,
       tl.owner,
       t.identifier,
       t.active,
       t.type,
       t.meta_data,
       t.display_order,
       tl.description
    FROM gateway_terminal gt
    JOIN terminal t ON gt.terminal_id=t.id
    LEFT JOIN terminal_localization tl ON tl.terminal_id = t.id 
      AND tl.language = :lang 
      WHERE gt.gateway_id=:gatewayId
    """
    )
    fun findByGatewayId(gatewayId: Long, lang: String? = null): Flux<TerminalView>?
}
