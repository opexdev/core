package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.otc.*
import co.nilin.opex.api.core.spi.RateProxy
import co.nilin.opex.api.ports.proxy.config.ProxyDispatchers
import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.body
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Component
class RateProxyImpl(@Qualifier("generalWebClient") private val webClient: WebClient) : RateProxy {

    private val logger by LoggerDelegate()

    @Value("\${app.wallet.url}")
    private lateinit var baseUrl: String

    // Rates
    override suspend fun createRate(token: String, request: SetCurrencyExchangeRateRequest) {
        withContext(ProxyDispatchers.wallet) {
            webClient.post()
                    .uri("$baseUrl/otc/rate")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(request))
                    .retrieve()
                    .toBodilessEntity()
                    .awaitFirstOrElse { null }
        }
    }

    override suspend fun updateRate(token: String, request: SetCurrencyExchangeRateRequest): Rates {
        return withContext(ProxyDispatchers.wallet) {
            webClient.put()
                    .uri("$baseUrl/otc/rate")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(request))
                    .retrieve()
                    .bodyToMono<Rates>()
                    .awaitSingle()
        }
    }

    override suspend fun deleteRate(token: String, sourceSymbol: String, destSymbol: String): Rates {
        return withContext(ProxyDispatchers.wallet) {
            webClient.delete()
                    .uri("$baseUrl/otc/rate/$sourceSymbol/$destSymbol")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    .retrieve()
                    .bodyToMono<Rates>()
                    .awaitSingle()
        }
    }

    override suspend fun fetchRates(): Rates {
        return withContext(ProxyDispatchers.wallet) {
            webClient.get()
                    .uri("$baseUrl/otc/rate")
                    .retrieve()
                    .bodyToMono<Rates>()
                    .awaitSingle()
        }
    }

    override suspend fun fetchRate(sourceSymbol: String, destSymbol: String): Rate? {
        return withContext(ProxyDispatchers.wallet) {
            webClient.get()
                    .uri("$baseUrl/otc/rate/$sourceSymbol/$destSymbol")
                    .retrieve()
                    .bodyToMono<Rate>()
                    .awaitFirstOrNull()
        }
    }

    // Forbidden pairs
    override suspend fun addForbiddenPair(token: String, request: CurrencyPair) {
        withContext(ProxyDispatchers.wallet) {
            webClient.post()
                    .uri("$baseUrl/otc/forbidden-pairs")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(request))
                    .retrieve()
                    .toBodilessEntity()
                    .awaitFirstOrElse { null }
        }
    }

    override suspend fun deleteForbiddenPair(token: String, sourceSymbol: String, destSymbol: String): ForbiddenPairs {
        return withContext(ProxyDispatchers.wallet) {
            webClient.delete()
                    .uri("$baseUrl/otc/forbidden-pairs/$sourceSymbol/$destSymbol")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    .retrieve()
                    .bodyToMono<ForbiddenPairs>()
                    .awaitSingle()
        }
    }

    override suspend fun fetchForbiddenPairs(): ForbiddenPairs {
        return withContext(ProxyDispatchers.wallet) {
            webClient.get()
                    .uri("$baseUrl/otc/forbidden-pairs")
                    .retrieve()
                    .bodyToMono<ForbiddenPairs>()
                    .awaitSingle()
        }
    }

    // Transitive symbols
    override suspend fun addTransitiveSymbols(token: String, symbols: Symbols) {
        withContext(ProxyDispatchers.wallet) {
            webClient.post()
                    .uri("$baseUrl/otc/transitive-symbols")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(symbols))
                    .retrieve()
                    .toBodilessEntity()
                    .awaitFirstOrElse { null }
        }
    }

    override suspend fun deleteTransitiveSymbol(token: String, symbol: String): Symbols {
        return withContext(ProxyDispatchers.wallet) {
            webClient.delete()
                    .uri("$baseUrl/otc/transitive-symbols/$symbol")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    .retrieve()
                    .bodyToMono<Symbols>()
                    .awaitSingle()
        }
    }

    override suspend fun deleteTransitiveSymbols(token: String, symbols: Symbols): Symbols {
        return withContext(ProxyDispatchers.wallet) {
            webClient.method(HttpMethod.DELETE)
                    .uri("$baseUrl/otc/transitive-symbols")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(symbols))
                    .retrieve()
                    .bodyToMono<Symbols>()
                    .awaitSingle()
        }
    }

    override suspend fun fetchTransitiveSymbols(): Symbols {
        return withContext(ProxyDispatchers.wallet) {
            webClient.get()
                    .uri("$baseUrl/otc/transitive-symbols")
                    .retrieve()
                    .bodyToMono<Symbols>()
                    .awaitSingle()
        }
    }

    // Routes and prices
    override suspend fun fetchRoutes(sourceSymbol: String?, destSymbol: String?): CurrencyExchangeRatesResponse {
        var uri = "$baseUrl/otc/route"
        if (sourceSymbol != null) uri = "$uri?sourceSymbol=$sourceSymbol"
        if (destSymbol != null) uri = "$uri?destSymbol=$destSymbol"
        return withContext(ProxyDispatchers.wallet) {
            webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono<CurrencyExchangeRatesResponse>()
                    .awaitSingle()
        }
    }

    override suspend fun getPrice(unit: String): List<CurrencyPrice> {
        return withContext(ProxyDispatchers.wallet) {
            webClient.get()
                    .uri("$baseUrl/otc/currency/price?unit=$unit")
                    .retrieve()
                    .bodyToFlux<CurrencyPrice>()
                    .collectList()
                    .awaitSingle()
        }
    }
}
