package co.nilin.opex.bcgateway.ports.postgres.dao

import co.nilin.opex.bcgateway.core.model.CurrencyOnChainGatewayView
import co.nilin.opex.bcgateway.core.model.WithdrawData
import co.nilin.opex.bcgateway.ports.postgres.model.CurrencyOnChainGatewayModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface CurrencyImplementationRepository : ReactiveCrudRepository<CurrencyOnChainGatewayModel, Long> {

    fun findByGatewayUuid(uuid: String): Mono<CurrencyOnChainGatewayModel>?

    @Query("""
    SELECT c.id,
       c.gateway_uuid,
       c.currency_symbol,
       c.implementation_symbol,
       c.chain,
       c.is_token,
       c.token_address,
       c.token_name,
       c.withdraw_allowed,
       c.deposit_allowed,
       c.withdraw_fee,
       c.withdraw_min,
       c.withdraw_max,
       c.deposit_min,
       c.deposit_max,
       c.decimal,
       c.is_deposit_active,
       c.is_withdraw_active,
       cl.deposit_description,
       cl.withdraw_description,
       c.display_order
    FROM currency_on_chain_gateway c
             LEFT JOIN public.currency_on_chain_gateway_localization cl ON c.id = cl.gateway_id AND cl.language = :lang
    where (:gatewayUuid is null or gateway_uuid=:gatewayUuid)
     and (:currencySymbol is null or currency_symbol=:currencySymbol )
     and (:implementationSymbol is null or implementation_symbol=:implementationSymbol )
     and (:chain is null or chain=:chain )
     order by display_order   
    """)

    fun findGateways(
        currencySymbol: String? = null,
        gatewayUuid: String? = null,
        chain: String? = null,
        implementationSymbol: String? = null
    ): Flux<CurrencyOnChainGatewayView>?

    fun deleteByGatewayUuid(uuid: String): Mono<Void>

    @Query(
        """
        select withdraw_enabled as is_enabled, withdraw_fee as fee, withdraw_min as minimum 
        from currency_on_chain_gateway 
        where implementation_symbol = :symbol and chain = :chain
    """
    )
    fun findWithdrawDataBySymbolAndChain(symbol: String, chain: String): Mono<WithdrawData>

    fun findByCurrencySymbolAndChain(symbol: String, chain: String): Mono<CurrencyOnChainGatewayModel>

    @Query("""
    SELECT c.id,
       c.gateway_uuid,
       c.currency_symbol,
       c.implementation_symbol,
       c.chain,
       c.is_token,
       c.token_address,
       c.token_name,
       c.withdraw_allowed,
       c.deposit_allowed,
       c.withdraw_fee,
       c.withdraw_min,
       c.withdraw_max,
       c.deposit_min,
       c.deposit_max,
       c.decimal,
       c.is_deposit_active,
       c.is_withdraw_active,
       cl.deposit_description,
       cl.withdraw_description,
       c.display_order
    FROM currency_on_chain_gateway c
             LEFT JOIN public.currency_on_chain_gateway_localization cl ON c.id = cl.gateway_id AND cl.language = :lang
    where c.gateway_uuid = :gatewayUuid
      and c.currency_symbol = :symbol;    
    """)
    fun findByGatewayUuidAndCurrencySymbol(gatewayUuid: String, symbol: String , lang : String): Mono<CurrencyOnChainGatewayView>?

    @Query("select * from currency_on_chain_gateway where chain = :chain and is_token is false")
    fun findMainAssetGateway(chain: String): Mono<CurrencyOnChainGatewayModel>

    @Query("select * from currency_on_chain_gateway where chain = :chain and is_token is true and token_address = :tokenAddress")
    fun findTokenGateway(chain: String, tokenAddress: String): Mono<CurrencyOnChainGatewayModel>
}