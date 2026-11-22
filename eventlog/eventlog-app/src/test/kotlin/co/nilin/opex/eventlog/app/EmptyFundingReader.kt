package co.nilin.opex.eventlog.app

import co.nilin.opex.eventlog.core.funding.*
import java.time.LocalDateTime

object EmptyFundingReader : FundingLogReader {
    override suspend fun findOperation(type: String, operationId: String) = emptyList<FundingRecord>()
    override suspend fun findBetween(from: LocalDateTime, to: LocalDateTime, limit: Int) = emptyList<FundingRecord>()
    override suspend fun findReportable(from: LocalDateTime, to: LocalDateTime, limit: Int) = emptyList<FundingRecord>()
}
