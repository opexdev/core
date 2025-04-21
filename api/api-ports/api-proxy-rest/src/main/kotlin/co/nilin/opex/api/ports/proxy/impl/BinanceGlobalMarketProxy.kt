package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.GlobalPrice
import co.nilin.opex.api.core.spi.GlobalMarketProxy
import co.nilin.opex.api.ports.proxy.config.ProxyDispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URLEncoder

@Component
class BinanceGlobalMarketProxy(
    @Value("\${app.binance.api-url}")
    val baseUrl: String,
    private val restTemplate: RestTemplate
) : GlobalMarketProxy {

    override suspend fun getPrices(symbols: List<String>): List<GlobalPrice> {
        // Binance encoding requires to change some of the Java's encoding model
        // https://binance-docs.github.io/apidocs/spot/en/#symbol-price-ticker
        val param = symbols.map { s -> "\"$s\"" }.toString().replace(" ", "")
        val uri = UriComponentsBuilder.fromUriString("$baseUrl/api/v3/ticker/price")
            .queryParam("symbols", URLEncoder.encode(param, Charsets.UTF_8).replace("%2C", ","))
            .build(true)
            .toUri()

        return withContext(ProxyDispatchers.general) {
            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                Array<GlobalPrice>::class.java
            ).body?.toList() ?: emptyList()
        }
    }
}