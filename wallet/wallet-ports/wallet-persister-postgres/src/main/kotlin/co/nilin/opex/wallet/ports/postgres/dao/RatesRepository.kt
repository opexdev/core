package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.ports.postgres.model.otc.RateModel
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface RatesRepository :ReactiveCrudRepository<RateModel,Long> {
    fun findBySourceSymbolAndDestinationSymbol(sourceSymb:String, destSymb:String):RateModel?

    fun deleteBySourceSymbolAndDestinationSymbol(sourceSymb:String, destSymb:String):Mono<Void>?

    @Query("select r from rate r where (sourceSymbol=:sourceSymb or :sourceSymb=\"all\") and (destinationSymbol=:destSymb or :destSymb=\"all\")")
    fun findRoutes(sourceSymb:String, destSymb:String): Flux<RateModel>?


}