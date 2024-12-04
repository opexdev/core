package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.TerminalModel
import co.nilin.opex.wallet.ports.postgres.model.GatewayTerminalModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface GatewayTerminalRepository : ReactiveCrudRepository<GatewayTerminalModel, Long> {

    fun deleteByTerminalIdAndGatewayId(terminalId: Long, gatewayId: Long): Mono<Void>

    @Query("select b.* from gateway_terminal gt join terminal t on gt.terminal_id=t.id where gt.gateway_id=:gatewayId ")
    fun findByGatewayId(gatewayId: Long): Flux<TerminalModel>?
}