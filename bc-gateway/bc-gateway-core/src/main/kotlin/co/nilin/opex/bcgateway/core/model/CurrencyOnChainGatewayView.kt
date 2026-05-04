package co.nilin.opex.bcgateway.core.model

import java.math.BigDecimal

data class CurrencyOnChainGatewayView(
    var id: Long? = null,
    val gatewayUuid: String,
    val currencySymbol: String,
    var implementationSymbol: String? = currencySymbol,
    var chain: String,
    var isToken: Boolean? = false,
    var tokenAddress: String? = null,
    var tokenName: String? = null,
    var withdrawAllowed: Boolean,
    var depositAllowed: Boolean,
    var withdrawFee: BigDecimal,
    var withdrawMin: BigDecimal? = BigDecimal.ZERO,
    var withdrawMax: BigDecimal? = BigDecimal.ZERO,
    var depositMin: BigDecimal? = BigDecimal.ZERO,
    var depositMax: BigDecimal? = BigDecimal.ZERO,
    var decimal: Int,
    var isDepositActive: Boolean? = true,
    var isWithdrawActive: Boolean? = true,
    val depositDescription: String? = null,
    val withdrawDescription: String? = null,
    val displayOrder: Int? = null
)


