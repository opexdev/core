package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.ManualGatewayModel
import co.nilin.opex.wallet.ports.postgres.model.OffChainGatewayModel
import co.nilin.opex.wallet.ports.postgres.model.RateModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface ManualGatewayRepository : ReactiveCrudRepository<ManualGatewayModel, Long> {

    fun findByGatewayUuid(uuid: String): Mono<ManualGatewayModel>?
    fun findByGatewayUuidAndCurrencySymbol(uuid: String,currencySymbol: String): Mono<ManualGatewayModel>?

    @Query("select * from currency_manual_gateway where (:gatewayUuid is null or gateway_uuid=:gatewayUuid) and (:currencySymbol is null or currency_symbol=:currencySymbol )  ")
    fun findGateways(currencySymbol: String? = null, gatewayUuid: String? = null): Flux<ManualGatewayModel>?

    fun deleteByGatewayUuid(uuid: String):Mono<Void>

}