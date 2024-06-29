package co.nilin.opex.market.app.controller

import co.nilin.opex.common.utils.Interval
import co.nilin.opex.market.core.inout.PriceStat
import co.nilin.opex.market.core.inout.TradeVolumeStat
import co.nilin.opex.market.core.inout.Transaction
import co.nilin.opex.market.core.inout.TxOfTrades
import co.nilin.opex.market.core.spi.MarketQueryHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("/v1/stats")
class MarketStatsController(private val marketQueryHandler: MarketQueryHandler) {

    @GetMapping("/price/most-increased")
    suspend fun getMostIncreasedPrices(@RequestParam interval: Interval, @RequestParam limit: Int): List<PriceStat> {
        return marketQueryHandler.mostIncreasePrice(interval, limit)
    }

    @GetMapping("/price/most-decreased")
    suspend fun getMostDecreasedPrices(@RequestParam interval: Interval, @RequestParam limit: Int): List<PriceStat> {
        return marketQueryHandler.mostDecreasePrice(interval, limit)
    }

    @GetMapping("/volume/highest")
    suspend fun getHighestVolume(@RequestParam interval: Interval): TradeVolumeStat? {
        return marketQueryHandler.mostVolume(interval)
    }

    @GetMapping("/most-trades")
    suspend fun getMostTrades(@RequestParam interval: Interval): TradeVolumeStat? {
        return marketQueryHandler.mostTrades(interval)
    }



}