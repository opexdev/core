package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.GlobalPrice
import co.nilin.opex.api.core.spi.GlobalMarketProxy
import co.nilin.opex.api.ports.proxy.config.ProxyDispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import org.springframework.web.util.UriComponentsBuilder
import java.net.URLEncoder

@Component
class BinanceGlobalMarketProxy(
    @Value("\${app.binance.api-url}")
    private val baseUrl: String,
) : GlobalMarketProxy {

    private val restTemplate = RestTemplate()

    override suspend fun getPrices(symbols: List<String>): List<GlobalPrice> {
        // Binance encoding requires to change some of the Java's encoding model
        // https://binance-docs.github.io/apidocs/spot/en/#symbol-price-ticker
        val param = symbols.map { s -> "\"$s\"" }.toString().replace(" ", "")
        val uri = UriComponentsBuilder.fromUriString("$baseUrl/api/v3/ticker/price")
            .queryParam("symbols", URLEncoder.encode(param, Charsets.UTF_8).replace("%2C", ","))
            .build(true)
            .toUri()

        return withContext(ProxyDispatchers.general) {
            restTemplate.getForEntity<Array<GlobalPrice>>(uri).body?.toList() ?: emptyList()
        }
    }
}