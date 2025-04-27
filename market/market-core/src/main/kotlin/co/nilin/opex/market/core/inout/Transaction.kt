package co.nilin.opex.market.core.inout

import java.math.BigDecimal
import java.time.LocalDateTime

data class Transaction(
    var createDate: LocalDateTime,
    var volume: BigDecimal,
    val transactionPrice: BigDecimal,
    var matchedPrice: BigDecimal,
    var side: String,
    var symbol: String,
    var fee: BigDecimal,
    var user: String? = null
)

data class TxOfTrades(var transactions: List<Transaction?>?)