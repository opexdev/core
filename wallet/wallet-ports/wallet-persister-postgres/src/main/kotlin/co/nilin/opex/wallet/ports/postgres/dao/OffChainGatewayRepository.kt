package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.core.inout.TransferMethod
import co.nilin.opex.wallet.ports.postgres.model.OffChainGatewayModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface OffChainGatewayRepository : ReactiveCrudRepository<OffChainGatewayModel, Long> {
    fun findByGatewayUuid(uuid: String): Mono<OffChainGatewayModel>?

    fun findByGatewayUuidAndCurrencySymbol(uuid: String, symbol: String): Mono<OffChainGatewayModel>?

    fun deleteByGatewayUuid(uuid: String): Mono<Void>

    @Query("select * from currency_off_chain_gateway where (:gatewayUuid is null or gateway_uuid=:gatewayUuid) and (:currencySymbol is null or currency_symbol=:currencySymbol ) order by display_order")
    fun findGateways(currencySymbol: String? = null, gatewayUuid: String? = null): Flux<OffChainGatewayModel>?

    fun findByCurrencySymbolAndAndTransferMethod(
        currencySymbol: String,
        transferMethod: TransferMethod
    ): Mono<OffChainGatewayModel>?

}
