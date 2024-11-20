package co.nilin.opex.bcgateway.core.model

import java.math.BigDecimal

data class CurrencyImplementation(
    val currency: String,
    val implCurrency: String,
    val chain: String,
    val token: Boolean,
    val tokenAddress: String?,
    val tokenName: String?,
    val withdrawEnabled: Boolean,
    val withdrawFee: BigDecimal,
    val withdrawMin: BigDecimal,
    val decimal: Int,
//    val chainDetail:Chain?=null,

)
