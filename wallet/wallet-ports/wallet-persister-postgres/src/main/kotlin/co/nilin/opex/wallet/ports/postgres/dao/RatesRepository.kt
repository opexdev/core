package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.RateModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface RatesRepository : ReactiveCrudRepository<RateModel, Long> {

    fun findBySourceSymbolAndDestinationSymbol(sourceSymbol: String, destSymbol: String): Mono<RateModel?>?

    fun deleteBySourceSymbolAndDestinationSymbol(sourceSymbol: String, destSymbol: String): Mono<Void>?


}