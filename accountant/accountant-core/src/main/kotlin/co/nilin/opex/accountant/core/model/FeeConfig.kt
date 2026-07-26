package co.nilin.opex.accountant.core.model

import java.math.BigDecimal

data class FeeConfig(
    val name: String,
    val displayOrder: Int,
    val minAssetVolume: BigDecimal,
    val maxAssetVolume: BigDecimal? = null,
    val minTradeVolume: BigDecimal,
    val maxTradeVolume: BigDecimal? = null,
    val makerFee: BigDecimal,
    val takerFee: BigDecimal,
    val condition: Condition
)
enum class Condition { AND, OR }