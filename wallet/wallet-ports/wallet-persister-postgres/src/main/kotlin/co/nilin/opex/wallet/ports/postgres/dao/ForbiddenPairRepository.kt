package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.ForbiddenPairModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface ForbiddenPairRepository :ReactiveCrudRepository<ForbiddenPairModel,Long>{


    fun findAllBy():Flux<ForbiddenPairModel>?


    fun findBySourceSymbolAndDestinationSymbol(sourceSymbol:String, destinationSymbol:String): Mono<ForbiddenPairModel>?

    fun deleteBySourceSymbolAndDestinationSymbol(sourceSymbol:String, destinationSymbol:String): Mono<Void>?


}