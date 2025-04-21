package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.PriceStat
import co.nilin.opex.api.core.inout.TradeVolumeStat
import co.nilin.opex.api.core.spi.MarketStatProxy
import co.nilin.opex.api.ports.proxy.config.ProxyDispatchers
import co.nilin.opex.common.utils.Interval
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Component
class MarketStatProxyImpl(
    private val restTemplate: RestTemplate,
    @Value("\${app.market.url}")
    private val baseUrl: String
) : MarketStatProxy {

    override suspend fun getMostIncreasedInPricePairs(interval: Interval, limit: Int): List<PriceStat> {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/stats/price/most-increased")
                .queryParam("interval", interval)
                .queryParam("limit", limit)
                .build()
                .toUri()

            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                Array<PriceStat>::class.java
            ).body?.toList() ?: emptyList()
        }
    }

    override suspend fun getMostDecreasedInPricePairs(interval: Interval, limit: Int): List<PriceStat> {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/stats/price/most-decreased")
                .queryParam("interval", interval)
                .queryParam("limit", limit)
                .build()
                .toUri()

            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                Array<PriceStat>::class.java
            ).body?.toList() ?: emptyList()
        }
    }

    override suspend fun getHighestVolumePair(interval: Interval): TradeVolumeStat? {
        // This method is not implemented in the new implementation
        return null
    }

    override suspend fun getTradeCountPair(interval: Interval): TradeVolumeStat? {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/stats/most-trades")
                .queryParam("interval", interval)
                .build()
                .toUri()

            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                TradeVolumeStat::class.java
            ).body
        }
    }
}