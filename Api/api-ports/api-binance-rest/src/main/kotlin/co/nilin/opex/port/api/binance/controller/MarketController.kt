package co.nilin.opex.port.api.binance.controller

import co.nilin.opex.api.core.spi.MarketQueryHandler
import co.nilin.opex.api.core.spi.SymbolMapper
import co.nilin.opex.port.api.binance.data.OrderBookResponse
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.utility.error.data.throwError
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import kotlin.collections.ArrayList

@RestController
class MarketController(
    private val marketQueryMarket: MarketQueryHandler,
    private val symbolMapper: SymbolMapper,
) {

    private val orderBookValidLimits = arrayListOf(5, 10, 20, 50, 100, 500, 1000, 5000)
    private val defaultOrderBookLimit = 100

    // Limit - Weight
    // 5, 10, 20, 50, 100 - 1
    // 500 - 5
    // 1000 - 10
    // 5000 - 50
    @GetMapping("/v3/depth")
    suspend fun orderBook(
        @RequestParam("symbol")
        symbol: String,
        @RequestParam("limit", required = false)
        limit: Int? // Default 100; max 5000. Valid limits:[5, 10, 20, 50, 100, 500, 1000, 5000]
    ): OrderBookResponse {
        val validLimit = limit ?: 100
        val localSymbol = symbolMapper.unmap(symbol) ?: throw OpexException(OpexError.SymbolNotFound)
        if (!orderBookValidLimits.contains(validLimit))
            throwError(OpexError.InvalidLimitForOrderBook)

        val mappedBidOrders = ArrayList<ArrayList<BigDecimal>>()
        val mappedAskOrders = ArrayList<ArrayList<BigDecimal>>()

        val bidOrders = marketQueryMarket.openBidOrders(localSymbol, validLimit)
        val askOrders = marketQueryMarket.openAskOrders(localSymbol, validLimit)

        bidOrders.forEach {
            val mapped = arrayListOf<BigDecimal>().apply {
                add(it.price)
                add(it.origQty)
            }
            mappedBidOrders.add(mapped)
        }

        askOrders.forEach {
            val mapped = arrayListOf<BigDecimal>().apply {
                add(it.price)
                add(it.origQty)
            }
            mappedAskOrders.add(mapped)
        }

        return OrderBookResponse(-1, mappedBidOrders, mappedAskOrders)
    }

}