package co.nilin.opex.bcgateway.app.dto

import java.math.BigDecimal

data class AddCurrencyRequest(
    var currencySymbol: String,
    var implementationSymbol: String,
    var currencyName: String,
    var newChain: String? = null,
    var tokenName: String?,
    var tokenAddress: String?,
    var isToken: Boolean? = false,
    var withdrawFee: BigDecimal,
    var minimumWithdraw: BigDecimal,
    var isWithdrawEnabled: Boolean? = true,
    var decimal: Int,
    var chain: String
)