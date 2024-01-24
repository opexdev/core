package co.nilin.opex.wallet.ports.proxy.bcgateway.impl

import co.nilin.opex.wallet.core.model.PropagateCurrencyChanges
import co.nilin.opex.wallet.core.model.otc.CurrencyImplementationResponse
import co.nilin.opex.wallet.core.model.otc.FetchCurrencyInfo
import co.nilin.opex.wallet.core.spi.BcGatewayProxy
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.net.URI
import org.springframework.security.core.context.SecurityContextHolder

inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

@Component
class BcGatewayProxyImpl(private val webClient: WebClient,
                         private val extractBackgroundAuth: ExtractBackgroundAuth) : BcGatewayProxy {

    @Value("\${app.bc-gateway.url}")
    private lateinit var baseUrl: String

    private val logger = LoggerFactory.getLogger(BcGatewayProxyImpl::class.java)

    override suspend fun createCurrency(currencyImp: PropagateCurrencyChanges): CurrencyImplementationResponse {
        val token = extractBackgroundAuth.extractToken()
        return webClient.post()
                .uri(URI.create("$baseUrl/currency/${currencyImp.currencySymbol}"))

                .headers { httpHeaders ->
                    run {
                        httpHeaders.add("Content-Type", "application/json");
                        token?.let { httpHeaders.add("Authorization", "Bearer $it") }
                    }
                }
                .bodyValue(currencyImp)
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono(typeRef<CurrencyImplementationResponse>())
                .log()
                .awaitFirst()
    }

    override suspend fun updateCurrency(currencyImp: PropagateCurrencyChanges): CurrencyImplementationResponse {
        val token = extractBackgroundAuth.extractToken()

        return webClient.put()
                .uri(URI.create("$baseUrl/currency/${currencyImp.currencySymbol}"))
                .headers { httpHeaders ->
                    run {
                        httpHeaders.add("Content-Type", "application/json");
                        token?.let { httpHeaders.add("Authorization", "Bearer $it") }
                    }
                }
                .bodyValue(currencyImp)
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono(typeRef<CurrencyImplementationResponse>())
                .log()
                .awaitFirst()
    }


    override suspend fun getCurrencyInfo(symbol: String): FetchCurrencyInfo? {
        val token = extractBackgroundAuth.extractToken()

        return webClient.get()
                .uri(URI.create("$baseUrl/currency/${symbol}"))
                .headers { httpHeaders ->
                    run {
                        httpHeaders.add("Content-Type", "application/json");
                        token?.let { httpHeaders.add("Authorization", "Bearer $it") }
                    }
                }
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono(typeRef<FetchCurrencyInfo>())
                .log()
                .awaitFirst()
    }
}
