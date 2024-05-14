package co.nilin.opex.bcgateway.ports.postgres.dao

import co.nilin.opex.bcgateway.ports.postgres.model.CurrencyImplementationModel
import co.nilin.opex.bcgateway.ports.postgres.model.NewCurrencyImplementationModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface NewCurrencyImplementationRepository : ReactiveCrudRepository<NewCurrencyImplementationModel, Long> {
    fun findByCurrencyImplUuid(uuid:String): Mono<NewCurrencyImplementationModel>?

    @Query("select * from new_currency where (:uuid=null or :uuid=uuid) and (:currencyUuid =null or currency_uuid=:currencyUuid ) and (:implementationSymbol =null or implementation_symbol=:implementationSymbol ) and (:chain =null or chain=:chain )  ")
    fun findImpls(uuid:String?,currencyUuid:String?,chain:String?,implementationSymbol:String?): Flux<NewCurrencyImplementationModel>?


}