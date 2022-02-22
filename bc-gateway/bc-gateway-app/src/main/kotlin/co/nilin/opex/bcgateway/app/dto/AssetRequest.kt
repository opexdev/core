package co.nilin.opex.bcgateway.app.dto

data class AssetRequest(
    val symbol: String?,
    val chain: String?,
    val tokenName: String?,
    val tokenAddress: String?,
    val isToken: Boolean,
    val withdrawFee: Double,
    val minimumWithdraw: Double,
    val isWithdrawEnabled: Boolean,
    val decimal: Int
)