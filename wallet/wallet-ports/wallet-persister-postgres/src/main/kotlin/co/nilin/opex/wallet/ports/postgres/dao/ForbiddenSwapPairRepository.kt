package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.ForbiddenSwapPairModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface ForbiddenSwapPairRepository : ReactiveCrudRepository<ForbiddenSwapPairModel, Long> {

    fun findAllBy(): Flux<ForbiddenSwapPairModel>?

    fun findBySourceSymbolAndDestinationSymbol(
        sourceSymbol: String,
        destinationSymbol: String
    ): Mono<ForbiddenSwapPairModel>?

    fun deleteBySourceSymbolAndDestinationSymbol(sourceSymbol: String, destinationSymbol: String): Mono<Void>?

}