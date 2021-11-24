package co.nilin.opex.port.websocket.service

import co.nilin.opex.port.websocket.dto.DepthResponse
import co.nilin.opex.port.websocket.dto.Interval
import co.nilin.opex.port.websocket.dto.RecentTradeResponse
import co.nilin.opex.websocket.core.inout.PriceChangeResponse
import co.nilin.opex.websocket.core.inout.PriceTickerResponse
import co.nilin.opex.websocket.core.spi.MarketQueryHandler
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.ZoneId

@Service
class MarketService(private val marketQueryHandler: MarketQueryHandler) {

    suspend fun getOrderBookDepth(symbol: String): DepthResponse {
        val mappedBidOrders = ArrayList<ArrayList<BigDecimal>>()
        val mappedAskOrders = ArrayList<ArrayList<BigDecimal>>()

        val bidOrders = marketQueryHandler.openBidOrders(symbol, 500)
        val askOrders = marketQueryHandler.openAskOrders(symbol, 500)

        bidOrders.forEach {
            val mapped = arrayListOf<BigDecimal>().apply {
                add(it.price ?: BigDecimal.ZERO)
                add(it.quantity ?: BigDecimal.ZERO)
            }
            mappedBidOrders.add(mapped)
        }

        askOrders.forEach {
            val mapped = arrayListOf<BigDecimal>().apply {
                add(it.price ?: BigDecimal.ZERO)
                add(it.quantity ?: BigDecimal.ZERO)
            }
            mappedAskOrders.add(mapped)
        }

        val lastOrder = marketQueryHandler.lastOrder(symbol)
        return DepthResponse(lastOrder?.orderId ?: -1, mappedBidOrders, mappedAskOrders)
    }

    suspend fun getCandleData(symbol: String, duration: String): List<List<Any>> {
        val i = Interval.findByLabel(duration) ?: return emptyList()

        val list = ArrayList<ArrayList<Any>>()
        marketQueryHandler.getCandleInfo(symbol, "${i.duration} ${i.unit}", null, null, 500)
            .forEach {
                list.add(
                    arrayListOf(
                        it.openTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        it.open.toString(),
                        it.high.toString(),
                        it.low.toString(),
                        it.close.toString(),
                        it.volume.toString(),
                        it.closeTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        it.quoteAssetVolume.toString(),
                        it.trades,
                        it.takerBuyBaseAssetVolume.toString(),
                        it.takerBuyQuoteAssetVolume.toString(),
                        "0.0"
                    )
                )
            }
        return list
    }

    suspend fun getPriceChange(): List<PriceTickerResponse> {
        return marketQueryHandler.lastPrice(null)
    }

    suspend fun getPriceOverview(symbol: String, duration: String): List<PriceChangeResponse> {
        val startDate = Interval.findByLabel(duration)?.getLocalDateTime() ?: Interval.Day.getLocalDateTime()
        return listOf(marketQueryHandler.getTradeTickerDataBySymbol(symbol, startDate))
    }

    suspend fun getRecentTrades(symbol: String): List<RecentTradeResponse> {
        return marketQueryHandler.recentTrades(symbol, 500)
            .map {
                RecentTradeResponse(
                    it.symbol,
                    it.id,
                    it.price,
                    it.qty,
                    it.quoteQty,
                    it.time,
                    it.isBestMatch,
                    it.isMakerBuyer
                )
            }
            .toList()
    }

}