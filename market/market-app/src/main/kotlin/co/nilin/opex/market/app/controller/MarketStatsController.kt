package co.nilin.opex.market.app.controller

import co.nilin.opex.market.app.utils.asLocalDateTime
import co.nilin.opex.market.core.inout.PriceStat
import co.nilin.opex.market.core.inout.TradeVolumeStat
import co.nilin.opex.market.core.spi.MarketQueryHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/v1/stats")
class MarketStatsController(private val marketQueryHandler: MarketQueryHandler) {

    @GetMapping("/price/most-increased")
    suspend fun getMostIncreasedPrices(@RequestParam interval: Long, @RequestParam limit: Int): List<PriceStat> {
        return marketQueryHandler.mostIncreasePrice(Date(interval).asLocalDateTime(), limit)
    }

    @GetMapping("/price/most-decreased")
    suspend fun getMostDecreasedPrices(@RequestParam interval: Long, @RequestParam limit: Int): List<PriceStat> {
        return marketQueryHandler.mostDecreasePrice(Date(interval).asLocalDateTime(), limit)
    }

    @GetMapping("/volume/highest")
    suspend fun getHighestVolume(@RequestParam interval: Long): TradeVolumeStat? {
        return marketQueryHandler.mostVolume(interval.asLocalDateTime())
    }

    @GetMapping("/most-trades")
    suspend fun getMostTrades(@RequestParam interval: Long): TradeVolumeStat? {
        return marketQueryHandler.mostTrades(interval.asLocalDateTime())
    }

}