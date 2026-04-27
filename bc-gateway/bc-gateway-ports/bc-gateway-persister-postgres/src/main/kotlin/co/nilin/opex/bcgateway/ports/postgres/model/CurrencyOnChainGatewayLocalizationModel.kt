package co.nilin.opex.bcgateway.ports.postgres.model


import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("currency_on_chain_gateway_localization")
data class CurrencyOnChainGatewayLocalizationModel(
    @Id var id: Long? = null,
    var gatewayId: Long,
    var depositDescription: String? = null,
    var withdrawDescription: String? = null,
    var language: String

)




