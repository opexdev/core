package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.TerminalModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TerminalRepository : ReactiveCrudRepository<TerminalModel, Long> {
    fun findByIdentifier(identifier: String): Mono<TerminalModel>?
    fun findByUuid(uuid: String): Mono<TerminalModel>?
    fun findAllByOrderByDisplayOrder(): Flux<TerminalModel>


}