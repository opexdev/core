package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.PriceStat
import co.nilin.opex.api.core.inout.TradeVolumeStat
import co.nilin.opex.api.core.spi.MarketStatProxy
import co.nilin.opex.api.ports.proxy.utils.noBody
import co.nilin.opex.common.utils.Interval
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import org.springframework.web.util.UriComponentsBuilder

@Component
class MarketStatProxyImpl(
    private val restTemplate: RestTemplate,
    @Value("\${app.market.url}")
    private val baseUrl: String
) : MarketStatProxy {

    override fun getMostIncreasedInPricePairs(interval: Interval, limit: Int): List<PriceStat> {
        val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/stats/price/most-increased")
            .queryParam("interval", interval)
            .queryParam("limit", limit)
            .build().toUri()
        return restTemplate.exchange<Array<PriceStat>>(uri, HttpMethod.GET, noBody()).body?.toList() ?: emptyList()
    }

    override fun getMostDecreasedInPricePairs(interval: Interval, limit: Int): List<PriceStat> {
        val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/stats/price/most-decreased")
            .queryParam("interval", interval)
            .queryParam("limit", limit)
            .build().toUri()
        return restTemplate.exchange<Array<PriceStat>>(uri, HttpMethod.GET, noBody()).body?.toList() ?: emptyList()
    }

    override fun getHighestVolumePair(interval: Interval): TradeVolumeStat? {
        val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/stats/volume/highest")
            .queryParam("interval", interval)
            .build().toUri()
        return restTemplate.exchange<TradeVolumeStat>(uri, HttpMethod.GET, noBody()).body
    }

    override fun getTradeCountPair(interval: Interval): TradeVolumeStat? {
        val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/stats/most-trades")
            .queryParam("interval", interval)
            .build().toUri()
        return restTemplate.exchange<TradeVolumeStat>(uri, HttpMethod.GET, noBody()).body
    }
}