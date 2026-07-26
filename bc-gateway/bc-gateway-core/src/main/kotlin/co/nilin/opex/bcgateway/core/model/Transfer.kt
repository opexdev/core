package co.nilin.opex.bcgateway.core.model

import java.math.BigDecimal
import java.math.BigInteger

data class Transfer(
    val txHash: String,
    val receiver: Wallet,
    val isTokenTransfer: Boolean,
    val amount: BigDecimal,
    val chain: String,
    val tokenAddress: String? = null
)
