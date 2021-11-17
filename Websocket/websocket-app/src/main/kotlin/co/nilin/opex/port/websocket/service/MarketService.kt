package co.nilin.opex.port.websocket.service

import co.nilin.opex.port.websocket.dto.DepthResponse
import co.nilin.opex.websocket.core.inout.OrderBookResponse
import co.nilin.opex.websocket.core.spi.MarketQueryHandler
import org.springframework.stereotype.Service
import java.math.BigDecimal

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

}