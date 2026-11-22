package co.nilin.opex.eventlog.app.controller

import co.nilin.opex.eventlog.core.funding.FundingLogReader
import co.nilin.opex.eventlog.core.funding.FundingRecord
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/internal/eventlog/funding")
class FundingEventLogController(private val reader: FundingLogReader) {
    @GetMapping("/events")
    suspend fun events(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: LocalDateTime,
        @RequestParam(defaultValue = "2000") limit: Int
    ): List<FundingRecord> = reader.findBetween(from, to, limit)

    @GetMapping("/operations/{type}/{operationId}")
    suspend fun operation(@PathVariable type: String, @PathVariable operationId: String): List<FundingRecord> =
        reader.findOperation(type.uppercase(), operationId)
}
