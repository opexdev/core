package co.nilin.opex.eventlog.core.funding

interface FundingLogWriter {
    suspend fun save(record: FundingRecord, topic: String, partition: Int, offset: Long): FundingRecord
}
