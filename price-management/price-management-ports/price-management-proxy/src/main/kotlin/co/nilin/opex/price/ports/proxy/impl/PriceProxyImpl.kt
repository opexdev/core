package co.nilin.opex.price.ports.proxy.impl

import co.nilin.opex.price.core.dto.ProviderPrice
import co.nilin.opex.price.core.dto.SymbolPrices
import co.nilin.opex.price.core.spi.PriceProxy
import co.nilin.opex.price.ports.proxy.dto.AllLatestPriceResponse
import co.nilin.opex.price.ports.proxy.utils.asCoreModel
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class PriceProxyImpl(
    private val priceProxyWebClient: WebClient
) : PriceProxy {

    private val logger = LoggerFactory.getLogger(PriceProxyImpl::class.java)

    override suspend fun getPrices(symbols: List<String>): List<SymbolPrices> {
        if (symbols.isEmpty()) return emptyList()

        val response = try {
            priceProxyWebClient.get()
                .uri("/public/price/latest")
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
        val response = try {
            priceProxyWebClient.get()
                .uri("/public/price/$symbol/latest")
                .retrieve()
                .bodyToMono(SymbolPrices::class.java)
                .awaitSingle()
        } catch (e: Exception) {
            logger.error("Failed to fetch prices from price proxy: ${e.message}", e)
            return emptyList()
        }

        return response.prices
    }
}
