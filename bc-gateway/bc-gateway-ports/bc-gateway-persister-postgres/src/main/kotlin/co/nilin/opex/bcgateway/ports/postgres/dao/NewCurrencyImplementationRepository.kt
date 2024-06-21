package co.nilin.opex.bcgateway.ports.postgres.dao

import co.nilin.opex.bcgateway.ports.postgres.model.CurrencyImplementationModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface NewCurrencyImplementationRepository : ReactiveCrudRepository<CurrencyImplementationModel, Long> {
    fun findByImplUuid(uuid:String): Mono<CurrencyImplementationModel>?

    @Query("select * from currency_implementations where (:impluuid is null or impl_uuid=:impluuid) and (:currencySymbol is null or currency_symbol=:currencySymbol ) and (:implementationSymbol is null or implementation_symbol=:implementationSymbol ) and (:chain is null or chain=:chain )  ")
    fun findImpls(currencySymbol:String?=null,implUuid:String?=null,chain:String?=null,implementationSymbol:String?=null): Flux<CurrencyImplementationModel>?


    fun deleteByImplUuid(uuid:String):Mono<Void>

}