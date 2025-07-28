package co.nilin.opex.wallet.core.inout

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.math.BigDecimal
import java.util.*

enum class GatewayType() {
    OnChain, OffChain
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = OffChainGatewayCommand::class, name = "OffChain"),
    JsonSubTypes.Type(value = OnChainGatewayCommand::class, name = "OnChain"),
)
open abstract class CurrencyGatewayCommand(
    open var currencySymbol: String? = null,
    open var gatewayUuid: String? = UUID.randomUUID().toString(),
    open var isDepositActive: Boolean?,
    open var isWithdrawActive: Boolean?,
    open var withdrawFee: BigDecimal? = BigDecimal.ZERO,
    open var withdrawAllowed: Boolean? = true,
    open var depositAllowed: Boolean? = true,
    open var depositMin: BigDecimal? = BigDecimal.ZERO,
    open var depositMax: BigDecimal? = BigDecimal.ZERO,
    open var withdrawMin: BigDecimal? = BigDecimal.ZERO,
    open var withdrawMax: BigDecimal? = BigDecimal.ZERO,
    open var description: String? = null,
    open var displayOrder: Int? = null,
)

data class OffChainGatewayCommand(
    var transferMethod: TransferMethod,
    override var currencySymbol: String? = null,
    override var gatewayUuid: String? = UUID.randomUUID().toString(),
    override var isDepositActive: Boolean? = true,
    override var isWithdrawActive: Boolean? = true,
    override var withdrawFee: BigDecimal? = BigDecimal.ZERO,
    override var withdrawAllowed: Boolean? = true,
    override var depositAllowed: Boolean? = true,
    override var depositMin: BigDecimal? = BigDecimal.ZERO,
    override var depositMax: BigDecimal? = BigDecimal.ZERO,
    override var withdrawMin: BigDecimal? = BigDecimal.ZERO,
    override var withdrawMax: BigDecimal? = BigDecimal.ZERO,
    override var description: String? = null,
    override var displayOrder: Int? = null,
) : CurrencyGatewayCommand(
    currencySymbol,
    gatewayUuid,
    isDepositActive,
    isWithdrawActive,
    withdrawFee,
    withdrawAllowed,
    depositAllowed,
    depositMin,
    depositMax,
    withdrawMin,
    withdrawMax,
    description,
    displayOrder
)

data class OnChainGatewayCommand(

    var implementationSymbol: String? = null,
    var tokenName: String? = null,
    var tokenAddress: String? = null,
    var isToken: Boolean? = false,
    var decimal: Int,
    var chain: String,
    override var currencySymbol: String? = null,
    override var gatewayUuid: String? = UUID.randomUUID().toString(),
    override var isDepositActive: Boolean? = true,
    override var isWithdrawActive: Boolean? = true,
    override var withdrawFee: BigDecimal? = BigDecimal.ZERO,
    override var withdrawAllowed: Boolean? = true,
    override var depositAllowed: Boolean? = true,
    override var depositMin: BigDecimal? = BigDecimal.ZERO,
    override var depositMax: BigDecimal? = BigDecimal.ZERO,
    override var withdrawMin: BigDecimal? = BigDecimal.ZERO,
    override var withdrawMax: BigDecimal? = BigDecimal.ZERO,
    override var description: String? = null,
    override var displayOrder: Int? = null,
) : CurrencyGatewayCommand(
    currencySymbol,
    gatewayUuid,
    isDepositActive,
    isWithdrawActive,
    withdrawFee,
    withdrawAllowed,
    depositAllowed,
    depositMin,
    depositMax,
    withdrawMin,
    withdrawMax,
    description,
    displayOrder
)


