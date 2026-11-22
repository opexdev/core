package co.nilin.opex.eventlog.core.trade

interface RichTradeLogWriter {
    suspend fun save(trade: RichTradeRecord, topic: String, partition: Int, offset: Long): RichTradeRecord
}
