package co.nilin.opex.eventlog.core.funding

import java.time.LocalDateTime

interface FundingLogReader {
    suspend fun findBetween(from: LocalDateTime, to: LocalDateTime, limit: Int): List<FundingRecord>
    suspend fun findReportable(from: LocalDateTime, to: LocalDateTime, limit: Int): List<FundingRecord>
    suspend fun findOperation(type: String, operationId: String): List<FundingRecord>
}
