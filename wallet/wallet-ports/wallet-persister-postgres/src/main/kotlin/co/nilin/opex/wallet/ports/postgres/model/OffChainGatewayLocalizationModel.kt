package co.nilin.opex.wallet.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("currency_off_chain_gateway_localization")
data class OffChainGatewayLocalizationModel(
    @Id var id: Long? = null,
    var gatewayId: Long,
    var depositDescription: String? = null,
    var withdrawDescription: String? = null,
    var language: String
)
