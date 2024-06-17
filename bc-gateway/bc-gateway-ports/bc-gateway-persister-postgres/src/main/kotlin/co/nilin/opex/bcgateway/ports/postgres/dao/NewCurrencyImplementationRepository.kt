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

    @Query("select * from currency where (:uuid is null or :impuuid=uuid) and (:currencyUuid is null or currency_uuid=:currencyUuid ) and (:implementationSymbol =null or implementation_symbol=:implementationSymbol ) and (:chain =null or chain=:chain )  ")
    fun findImpls(implUuid:String?,currencySymbol:String?,chain:String?,implementationSymbol:String?): Flux<CurrencyImplementationModel>?


    fun deleteByImplUuid(uuid:String):Mono<Void>

}