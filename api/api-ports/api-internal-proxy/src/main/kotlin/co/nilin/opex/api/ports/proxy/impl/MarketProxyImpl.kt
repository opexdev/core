package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.MarketQueryHandler
import co.nilin.opex.api.core.spi.UserQueryHandler
import co.nilin.opex.api.core.utils.LoggerDelegate
import kotlinx.coroutines.flow.Flow
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.security.Principal
import java.time.LocalDateTime

@Component
class MarketProxyImpl(private val webClient: WebClient):UserQueryHandler,MarketQueryHandler {

    private val logger by LoggerDelegate()

    @Value("\${app.wallet.url}")
    private lateinit var baseUrl: String

    override suspend fun queryOrder(principal: Principal, request: QueryOrderRequest): QueryOrderResponse? {
        TODO("Not yet implemented")
    }

    override suspend fun openOrders(principal: Principal, symbol: String?): Flow<QueryOrderResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun allOrders(principal: Principal, allOrderRequest: AllOrderRequest): Flow<QueryOrderResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun allTrades(principal: Principal, request: TradeRequest): Flow<TradeResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getTradeTickerData(startFrom: LocalDateTime): List<PriceChangeResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getTradeTickerDataBySymbol(symbol: String, startFrom: LocalDateTime): PriceChangeResponse {
        TODO("Not yet implemented")
    }

    override suspend fun openBidOrders(symbol: String, limit: Int): List<OrderBookResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun openAskOrders(symbol: String, limit: Int): List<OrderBookResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun lastOrder(symbol: String): QueryOrderResponse? {
        TODO("Not yet implemented")
    }

    override suspend fun recentTrades(symbol: String, limit: Int): Flow<MarketTradeResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun lastPrice(symbol: String?): List<PriceTickerResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getCandleInfo(
        symbol: String,
        interval: String,
        startTime: Long?,
        endTime: Long?,
        limit: Int
    ): List<CandleData> {
        TODO("Not yet implemented")
    }

}