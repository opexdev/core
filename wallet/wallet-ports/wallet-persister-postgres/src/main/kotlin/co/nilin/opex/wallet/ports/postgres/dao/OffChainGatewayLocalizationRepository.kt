package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.OffChainGatewayLocalizationModel
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface OffChainGatewayLocalizationRepository : ReactiveCrudRepository<OffChainGatewayLocalizationModel, Long> {

    @Modifying
    @Query(
        """
    INSERT INTO currency_off_chain_gateway_localization (gateway_id, deposit_description, withdraw_description, language)
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

    fun findByGatewayId(gatewayId: Long): Flux<OffChainGatewayLocalizationModel>
}
