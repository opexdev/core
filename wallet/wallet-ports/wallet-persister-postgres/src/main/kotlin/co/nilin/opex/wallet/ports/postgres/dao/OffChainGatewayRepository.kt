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
        select g.id as id,
           g.gateway_uuid as gatewayUuid,
           g.currency_symbol as currencySymbol,
           g.withdraw_allowed as withdrawAllowed,
           g.deposit_allowed as depositAllowed,
           g.withdraw_fee as withdrawFee,
           g.withdraw_min as withdrawMin,
           g.withdraw_max as withdrawMax,
           g.deposit_min as depositMin,
           g.deposit_max as depositMax,
           g.transfer_method as transferMethod,
           g.is_deposit_active as isDepositActive,
           g.is_withdraw_active as isWithdrawActive,
           gl.deposit_description as depositDescription,
           gl.withdraw_description as withdrawDescription,
           g.display_order as displayOrder
    from currency_off_chain_gateway g
             left join currency_off_chain_gateway_localization gl
                    on g.id = gl.gateway_id
                   and gl.language = :lang
    where g.currency_symbol = :currencySymbol
      and g.gateway_uuid = :gatewayUuid
    """)
    fun findByGatewayUuidAndCurrencySymbol(
        gatewayUuid: String,
        currencySymbol: String,
        lang: String? = UserLanguage.getDefaultLanguage()
    ): Mono<OffChainGatewayView>?

    fun deleteByGatewayUuid(uuid: String): Mono<Void>

    @Query("""
       select g.id as id,
           g.gateway_uuid as gatewayUuid,
           g.currency_symbol as currencySymbol,
           g.withdraw_allowed as withdrawAllowed,
           g.deposit_allowed as depositAllowed,
           g.withdraw_fee as withdrawFee,
           g.withdraw_min as withdrawMin,
           g.withdraw_max as withdrawMax,
           g.deposit_min as depositMin,
           g.deposit_max as depositMax,
           g.transfer_method as transferMethod,
           g.is_deposit_active as isDepositActive,
           g.is_withdraw_active as isWithdrawActive,
           gl.deposit_description as depositDescription,
           gl.withdraw_description as withdrawDescription,
           g.display_order as displayOrder
    from currency_off_chain_gateway g
             left join currency_off_chain_gateway_localization gl
                    on g.id = gl.gateway_id
                   and gl.language = :language
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
