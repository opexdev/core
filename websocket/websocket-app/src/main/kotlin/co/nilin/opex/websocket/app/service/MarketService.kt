package co.nilin.opex.websocket.app.service

import co.nilin.opex.websocket.app.dto.DepthResponse
import co.nilin.opex.websocket.app.dto.Interval
import co.nilin.opex.websocket.app.proxy.MarketProxy
import co.nilin.opex.websocket.core.inout.*
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.ZoneId

@Service
class MarketService(private val marketProxy: MarketProxy) {

    suspend fun getOrderBookDepth(symbol: String): DepthResponse {
        val mappedBidOrders = ArrayList<ArrayList<BigDecimal>>()
        val mappedAskOrders = ArrayList<ArrayList<BigDecimal>>()

        val bidOrders = marketProxy.openBidOrders(symbol, 500)
        val askOrders = marketProxy.openAskOrders(symbol, 500)

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

        val lastOrder = marketProxy.lastOrder(symbol)
        return DepthResponse(lastOrder?.orderId ?: -1, mappedBidOrders, mappedAskOrders)
    }

    suspend fun getCandleData(symbol: String, duration: String): List<List<Any>> {
        val i = Interval.findByLabel(duration) ?: return emptyList()

        val list = ArrayList<ArrayList<Any>>()
        marketProxy.getCandleInfo(symbol, "${i.duration} ${i.unit}", null, null, 500)
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

    suspend fun getPriceChange(): List<PriceTicker> {
        return marketProxy.lastPrice(null)
    }

    suspend fun getPriceOverview(symbol: String, duration: String): List<PriceChange> {
        val startDate = Interval.findByLabel(duration)?.getDate()?.time ?: Interval.Week.getDate().time
        return listOf(marketProxy.getTradeTickerDataBySymbol(symbol, startDate))
    }

    suspend fun getRecentTrades(symbol: String): List<MarketTrade> {
        return marketProxy.recentTrades(symbol, 500)
    }

}