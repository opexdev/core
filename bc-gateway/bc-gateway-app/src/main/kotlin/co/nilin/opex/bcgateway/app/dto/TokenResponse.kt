package co.nilin.opex.bcgateway.app.dto

import java.math.BigDecimal

data class TokenResponse(
    val currency: String,
    val chain: String,
    val isToken: Boolean,
    val tokenAddress: String?,
    val tokenName: String?,
    val isWithdrawEnabled: Boolean,
    val withdrawFee: BigDecimal,
    val withdrawMin: BigDecimal,
    val decimal: Int,
    val isActive: Boolean
)