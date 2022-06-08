package co.nilin.opex.bcgateway.app.dto

import java.math.BigDecimal

data class TokenRequest(
    val symbol: String?,
    val chain: String?,
    val isToken: Boolean,
    val tokenName: String?,
    val tokenAddress: String?,
    val tokenSymbol: String?,
    val withdrawFee: BigDecimal,
    val minimumWithdraw: BigDecimal,
    val isWithdrawEnabled: Boolean = true,
    val decimal: Int = 18
)
