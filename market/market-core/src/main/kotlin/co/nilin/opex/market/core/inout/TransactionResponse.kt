package co.nilin.opex.market.core.inout

import java.math.BigDecimal
import java.util.*


data class TransactionDto(
    var createDate: Date,
    var volume: BigDecimal,
    val transactionPrice: BigDecimal,
    var matchedPrice: BigDecimal,
    var side: String,
    var symbol: String,
    var fee: BigDecimal,
    var user: String? = null
)

data class TransactionResponse(var transactions: List<TransactionDto?>?)