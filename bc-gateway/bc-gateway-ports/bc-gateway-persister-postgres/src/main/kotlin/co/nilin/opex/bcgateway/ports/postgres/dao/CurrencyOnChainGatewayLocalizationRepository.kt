package co.nilin.opex.bcgateway.ports.postgres.dao

import co.nilin.opex.bcgateway.ports.postgres.model.CurrencyOnChainGatewayLocalizationModel
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface CurrencyOnChainGatewayLocalizationRepository :
    ReactiveCrudRepository<CurrencyOnChainGatewayLocalizationModel, Long> {
    @Modifying
    @Query(
        """
    INSERT INTO currency_on_chain_gateway_localization (gateway_id, deposit_description, withdraw_description, language)
    VALUES (:gatewayId, :depositDescription, :withdrawDescription, :language)
    ON CONFLICT (gateway_id, language)
    DO UPDATE SET
        deposit_description = :depositDescription,
        withdraw_description = :withdrawDescription
"""
    )
    fun upsert(
        gatewayId: Long,
        depositDescription: String?,
        withdrawDescription: String?,
        language: String
    ): Mono<Void>

    suspend fun findByGatewayId(gatewayId: Long): Flux<CurrencyOnChainGatewayLocalizationModel>
}