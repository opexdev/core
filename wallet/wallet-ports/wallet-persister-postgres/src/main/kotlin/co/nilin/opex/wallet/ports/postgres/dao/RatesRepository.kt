package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.RateModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface RatesRepository :ReactiveCrudRepository<RateModel,Long> {
    fun findBySourceSymbolAndDestinationSymbol(sourceSymbol:Long, destSymbol:Long): Mono<RateModel?>?

    fun deleteBySourceSymbolAndDestinationSymbol(sourceSymbol:Long, destSymbol:Long):Mono<Void>?




}