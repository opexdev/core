package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.common.data.UserLanguage
import co.nilin.opex.wallet.core.inout.TransferMethod
import co.nilin.opex.wallet.ports.postgres.dto.OffChainGatewayView
import co.nilin.opex.wallet.ports.postgres.model.OffChainGatewayModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface OffChainGatewayRepository : ReactiveCrudRepository<OffChainGatewayModel, Long> {
    fun findByGatewayUuid(uuid: String): Mono<OffChainGatewayModel>?

    @Query("""
    select g.id,
       g.gateway_uuid,
       g.currency_symbol,
       g.withdraw_allowed,
       g.deposit_allowed,
       g.withdraw_fee,
       g.withdraw_min,
       g.withdraw_max,
       g.deposit_min,
       g.deposit_max,
       g.transfer_method,
       g.is_deposit_active,
       g.is_withdraw_active,
       gl.deposit_description,
       gl.withdraw_description,
       g.display_order
    from currency_off_chain_gateway g
             left join currency_off_chain_gateway_localization gl on g.id = gl.gateway_id and gl.language = :language
    where g.currency_symbol = :currencySymbol and g.gateway_uuid = :gatewayUuid
    """)
    fun findByGatewayUuidAndCurrencySymbol(
        gatewayUuid: String,
        currencySymbol: String,
        language: String? = UserLanguage.getDefaultLanguage()
    ): Mono<OffChainGatewayView>?

    fun deleteByGatewayUuid(uuid: String): Mono<Void>

    @Query("""
    select g.id,
       g.gateway_uuid,
       g.currency_symbol,
       g.withdraw_allowed,
       g.deposit_allowed,
       g.withdraw_fee,
       g.withdraw_min,
       g.withdraw_max,
       g.deposit_min,
       g.deposit_max,
       g.transfer_method,
       g.is_deposit_active,
       g.is_withdraw_active,
       gl.deposit_description,
       gl.withdraw_description,
       g.display_order
    from currency_off_chain_gateway g
             left join currency_off_chain_gateway_localization gl on g.id = gl.gateway_id and gl.language = :lang
    where (:currencySymbol is null or g.currency_symbol = :currencySymbol)
      and (:gatewayUuid is null or g.gateway_uuid = :gatewayUuid)
    order by g.display_order
    """)
    fun findGateways(
        currencySymbol: String? = null,
        gatewayUuid: String? = null,
        language: String? = UserLanguage.getDefaultLanguage()
    ): Flux<OffChainGatewayView>?

    fun findByCurrencySymbolAndAndTransferMethod(
        currencySymbol: String,
        transferMethod: TransferMethod
    ): Mono<OffChainGatewayModel>?

}
