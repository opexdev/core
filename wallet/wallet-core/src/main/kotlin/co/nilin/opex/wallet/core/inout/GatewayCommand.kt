package co.nilin.opex.wallet.core.inout

import co.nilin.opex.common.OpexError
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.math.BigDecimal
import java.util.UUID

enum class GatewayType() {
    OnChain, OffChain, Manually
}

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes(JsonSubTypes.Type(value = OffChainGatewayCommand::class, name = "OffChain"),
        JsonSubTypes.Type(value = OnChainGatewayCommand::class, name = "OnChain"),
        JsonSubTypes.Type(value = ManualGatewayCommand::class, name = "Manually"))
abstract class CurrencyGatewayCommand(
        var type: GatewayType,
        var currencySymbol: String? = null,
        var gatewayUuid: String? = UUID.randomUUID().toString(),
        var isActive: Boolean? = true,
        var withdrawFee: BigDecimal? = BigDecimal.ZERO,
        var withdrawAllowed: Boolean? = true,
        var depositAllowed: Boolean? = true,
        var depositMin: BigDecimal? = BigDecimal.ZERO,
        var depositMax: BigDecimal? = BigDecimal.ZERO,
        var withdrawMin: BigDecimal? = BigDecimal.ZERO,
        var withdrawMax: BigDecimal? = BigDecimal.ZERO,
)

data class OffChainGatewayCommand(var transferMethod: TransferMethod) : CurrencyGatewayCommand(GatewayType.OffChain)
data class ManualGatewayCommand(var allowedFor: String) : CurrencyGatewayCommand(GatewayType.Manually)
data class OnChainGatewayCommand(
        var implementationSymbol: String? = null,
        var tokenName: String? = null,
        var tokenAddress: String? = null,
        var isToken: Boolean? = false,
        var decimal: Int,
        var chain: String) : CurrencyGatewayCommand(GatewayType.OnChain)

data class CurrencyGateways(var gateways: List<CurrencyGatewayCommand>?)
