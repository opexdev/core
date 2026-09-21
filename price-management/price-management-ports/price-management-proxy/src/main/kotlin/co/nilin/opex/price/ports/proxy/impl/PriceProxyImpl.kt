package co.nilin.opex.price.ports.proxy.impl

import co.nilin.opex.price.core.dto.ProviderPrice
import co.nilin.opex.price.core.dto.SymbolPrices
import co.nilin.opex.price.core.spi.PriceProxy
import co.nilin.opex.price.ports.proxy.dto.AllLatestPriceResponse
import co.nilin.opex.price.ports.proxy.dto.SymbolPriceResponse
import co.nilin.opex.price.ports.proxy.security.RequestSigner
import co.nilin.opex.price.ports.proxy.utils.asCoreModel
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class PriceProxyImpl(
    private val priceProxyWebClient: WebClient,
    @Value("\${app.price-proxy.secret-key}") private val secretKey: String
) : PriceProxy {

    private val logger = LoggerFactory.getLogger(PriceProxyImpl::class.java)

    override suspend fun getPrices(symbols: List<String>): List<SymbolPrices> {
        if (symbols.isEmpty()) return emptyList()

        val path = "/public/price/latest"
        val response = try {
            priceProxyWebClient.get()
                .uri(path)
                .headers { signRequest(it, HttpMethod.GET, path) }
                .retrieve()
                .bodyToMono(AllLatestPriceResponse::class.java)
                .awaitSingle()
        } catch (e: Exception) {
            logger.error("Failed to fetch prices from price proxy: ${e.message}", e)
            return emptyList()
        }

        val requestedSymbols = symbols.toSet()
        return response.items
            .filter { it.symbol in requestedSymbols }
            .map { it.asCoreModel() }
    }

    override suspend fun getProvidersPrice(symbol: String): List<ProviderPrice> {
        val path = "/public/price/$symbol/latest"
        val response = try {
            priceProxyWebClient.get()
                .uri(path)
                .headers { signRequest(it, HttpMethod.GET, path) }
                .retrieve()
                .bodyToMono(SymbolPriceResponse::class.java)
                .awaitSingle()
        } catch (e: Exception) {
            logger.error("Failed to fetch prices from price proxy: ${e.message}", e)
            return emptyList()
        }

        return response.asCoreModel().prices
    }

    // rate-scanner now requires HMAC signing on top of X-API-Key (already a default header on
    // priceProxyWebClient): X-Timestamp + X-Signature over "timestamp\nMETHOD\npath\nrawQuery\n
    // sha256hex(body)". None of this client's calls carry a query string or a body, so those two
    // segments of the canonical payload are always empty — update RequestSigner's caller here if a
    // future endpoint needs either.
    private fun signRequest(headers: HttpHeaders, method: HttpMethod, path: String) {
        val timestamp = System.currentTimeMillis()
        val signature = RequestSigner.sign(
            secretKey = secretKey,
            timestamp = timestamp,
            method = method.name(),
            path = path,
            rawQuery = "",
            body = ByteArray(0)
        )
        headers.set("X-Timestamp", timestamp.toString())
        headers.set("X-Signature", signature)
    }
}
