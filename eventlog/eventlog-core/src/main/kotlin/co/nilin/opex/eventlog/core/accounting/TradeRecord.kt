package co.nilin.opex.eventlog.core.accounting

import java.time.LocalDateTime

data class TradeRecord(
    val id: Long?,
    val symbol: String,
    val takerUuid: String,
    val makerUuid: String,
    val takerPrice: Long,
    val matchedQuantity: Long,
    val tradeDate: LocalDateTime
)
