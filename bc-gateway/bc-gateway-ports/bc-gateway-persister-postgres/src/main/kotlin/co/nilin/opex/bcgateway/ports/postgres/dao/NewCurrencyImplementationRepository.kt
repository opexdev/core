package co.nilin.opex.bcgateway.ports.postgres.dao

import co.nilin.opex.bcgateway.ports.postgres.model.CurrencyOnChainGatewayModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface NewCurrencyImplementationRepository : ReactiveCrudRepository<CurrencyOnChainGatewayModel, Long> {
    fun findByGatewayUuid(uuid:String): Mono<CurrencyOnChainGatewayModel>?

    @Query("select * from currency_on_chain_gateway where (:gatewayUuid is null or gateway_uuid=:gatewayUuid) and (:currencySymbol is null or currency_symbol=:currencySymbol ) and (:implementationSymbol is null or implementation_symbol=:implementationSymbol ) and (:chain is null or chain=:chain )  ")
    fun findGateways(currencySymbol:String?=null, gatewayUuid:String?=null, chain:String?=null, implementationSymbol:String?=null): Flux<CurrencyOnChainGatewayModel>?

    fun deleteByGatewayUuid(uuid:String):Mono<Void>

}