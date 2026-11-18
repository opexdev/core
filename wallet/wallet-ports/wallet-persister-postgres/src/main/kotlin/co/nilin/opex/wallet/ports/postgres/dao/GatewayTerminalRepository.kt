package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.common.data.UserLanguage
import co.nilin.opex.wallet.ports.postgres.dto.OffChainGatewayView
import co.nilin.opex.wallet.ports.postgres.dto.TerminalView
import co.nilin.opex.wallet.ports.postgres.model.GatewayTerminalModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface GatewayTerminalRepository : ReactiveCrudRepository<GatewayTerminalModel, Long> {

    @Modifying
    @Query("""
        INSERT INTO gateway_terminal (terminal_id, gateway_id, applicator)
        VALUES (:terminalId, :gatewayId, :applicator)
        ON CONFLICT (terminal_id, gateway_id) DO UPDATE SET applicator = :applicator
    """)
    fun upsert(terminalId: Long, gatewayId: Long, applicator: String): Mono<Void>

    fun deleteByTerminalIdAndGatewayId(terminalId: Long, gatewayId: Long): Mono<Void>

    @Query(
        """
    SELECT t.id,
       t.uuid,
       tl.owner,
       t.identifier,
       t.active,
       t.meta_data,
       t.display_order,
       tl.description,
       t.applicator
    FROM gateway_terminal gt
    JOIN terminal t ON gt.terminal_id=t.id
    LEFT JOIN terminal_localization tl ON tl.terminal_id = t.id 
      AND tl.language = :lang 
      WHERE gt.gateway_id=:gatewayId
    """
    )
    fun findByGatewayId(gatewayId: Long, lang: String? = UserLanguage.getDefaultLanguage()): Flux<TerminalView>?


    @Query("""
    select g.id,
       g.gateway_uuid,
       g.currency_symbol,
       g.deposit_allowed,
       g.withdraw_fee,
       g.withdraw_min,
       g.withdraw_max,
       g.deposit_min,
       g.deposit_max,
       g.transfer_method,
       g.is_deposit_active,
       g.is_withdraw_active,
       gl.deposit_description,
       gl.withdraw_description,
       g.display_order,
       g.applicator
    from gateway_terminal gt
             left join currency_off_chain_gateway g on gt.gateway_id = g.id
             left join currency_off_chain_gateway_localization gl on g.id = gl.gateway_id and gl.language = :lang
    where gt.terminal_id = :terminalId
    """)
    fun findByTerminalId(terminalId: Long, lang: String? = UserLanguage.getDefaultLanguage()): Flux<OffChainGatewayView>?
}
