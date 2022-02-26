package co.nilin.opex.bcgateway.app.dto

data class TokenRequest(
    val symbol: String?,
    val chain: String?,
    val isToken: Boolean,
    val tokenName: String?,
    val tokenAddress: String?,
    val withdrawFee: Double,
    val minimumWithdraw: Double,
    val isWithdrawEnabled: Boolean = true,
    val decimal: Int = 18
)