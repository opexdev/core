package co.nilin.opex.price.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.price.core.dto.AllPriceHistoryResponse
import co.nilin.opex.price.core.dto.HistoryRangeRequest
import co.nilin.opex.price.core.dto.LatestPriceResponse
import co.nilin.opex.price.core.dto.PriceHistoryResponse
import co.nilin.opex.price.core.dto.PricePoint
import co.nilin.opex.price.core.dto.RateHistory
import co.nilin.opex.price.core.spi.RateHistoryLoader
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/prices")
class PriceController(
    private val rateHistoryLoader: RateHistoryLoader
) {

    @GetMapping("/latest")
    suspend fun getAllLatest(): List<LatestPriceResponse> =
        rateHistoryLoader.loadLatestForAllSymbols().map { it.toLatestPriceResponse() }

    @GetMapping("/{symbol}/latest")
    suspend fun getLatest(@PathVariable symbol: String): LatestPriceResponse {
        val latest = rateHistoryLoader.loadLatest(symbol)
            ?: throw OpexError.PriceNotFound.exception()
        return latest.toLatestPriceResponse()
    }

    @PostMapping("/{symbol}/history")
    suspend fun getHistory(
        @PathVariable symbol: String,
        @RequestBody request: HistoryRangeRequest
    ): PriceHistoryResponse {
        requireValidRange(request.startTime, request.endTime)
        val items = rateHistoryLoader.loadHistory(symbol, request.startTime, request.endTime).map { it.toPricePoint() }
        return PriceHistoryResponse(symbol, request.startTime, request.endTime, items)
    }

    @PostMapping("/history")
    suspend fun getAllHistory(@RequestBody request: HistoryRangeRequest): AllPriceHistoryResponse {
        requireValidRange(request.startTime, request.endTime)
        val items = rateHistoryLoader.loadHistoryForAllSymbols(request.startTime, request.endTime)
            .groupBy { it.symbol }
            .map { (symbol, rows) ->
                PriceHistoryResponse(symbol, request.startTime, request.endTime, rows.map { it.toPricePoint() })
            }
        return AllPriceHistoryResponse(request.startTime, request.endTime, items)
    }

    private fun requireValidRange(startTime: LocalDateTime, endTime: LocalDateTime) {
        if (startTime.isAfter(endTime)) {
            throw OpexError.InvalidTimeRange.exception()
        }
    }

    private fun RateHistory.toLatestPriceResponse() = LatestPriceResponse(symbol, price, createdDate)

    private fun RateHistory.toPricePoint() = PricePoint(price, createdDate)
}
