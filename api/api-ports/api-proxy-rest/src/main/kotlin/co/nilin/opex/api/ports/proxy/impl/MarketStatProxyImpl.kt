package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.PriceStat
import co.nilin.opex.api.core.inout.TradeVolumeStat
import co.nilin.opex.api.core.spi.MarketStatProxy
import co.nilin.opex.api.ports.proxy.config.ProxyDispatchers
import co.nilin.opex.common.utils.Interval
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class MarketStatProxyImpl(
    @Qualifier("generalWebClient")
    private val webClient: WebClient,
    @Value("\${app.market.url}")
    private val baseUrl: String
) : MarketStatProxy {

    override suspend fun getMostIncreasedInPricePairs(interval: Interval, limit: Int): List<PriceStat> {
        return withContext(ProxyDispatchers.market) {
            webClient.get()
                .uri("$baseUrl/v1/stats/price/most-increased") {
                    it.queryParam("interval", interval)
                    it.queryParam("limit", limit)
                    it.build()
                }.accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToFlux<PriceStat>()
                .collectList()
                .awaitFirstOrElse { emptyList() }
        }
    }

    override suspend fun getMostDecreasedInPricePairs(interval: Interval, limit: Int): List<PriceStat> {
        return withContext(ProxyDispatchers.market) {
            webClient.get()
                .uri("$baseUrl/v1/stats/price/most-decreased") {
                    it.queryParam("interval", interval)
                    it.queryParam("limit", limit)
                    it.build()
                }.accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToFlux<PriceStat>()
                .collectList()
                .awaitFirstOrElse { emptyList() }
        }
    }

    override suspend fun getHighestVolumePair(interval: Interval): TradeVolumeStat? {
        return withContext(ProxyDispatchers.market) {
            webClient.get()
                .uri("$baseUrl/v1/stats/volume/highest") {
                    it.queryParam("interval", interval)
                    it.build()
                }.accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono<TradeVolumeStat>()
                .awaitSingleOrNull()
        }
    }

    override suspend fun getTradeCountPair(interval: Interval): TradeVolumeStat? {
        return withContext(ProxyDispatchers.market) {
            webClient.get()
                .uri("$baseUrl/v1/stats/most-trades") {
                    it.queryParam("interval", interval)
                    it.build()
                }.accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono<TradeVolumeStat>()
                .awaitSingleOrNull()
        }
    }
}