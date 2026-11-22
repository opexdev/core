package co.nilin.opex.eventlog.core.swap

interface SwapLogWriter {
    suspend fun save(swap: SwapRecord, topic: String, partition: Int, offset: Long): SwapRecord
}
