package co.nilin.opex.wallet.ports.proxy.bcgateway.impl

import co.nilin.opex.wallet.core.inout.WithdrawData
import co.nilin.opex.wallet.core.model.PropagateCurrencyChanges
import co.nilin.opex.wallet.core.model.otc.CurrencyImplementationResponse
import co.nilin.opex.wallet.core.model.otc.FetchCurrencyInfo
import co.nilin.opex.wallet.core.spi.BcGatewayProxy
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.net.URI

inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

@Component
class BcGatewayProxyImpl(
    private val webClient: WebClient,
    private val extractBackgroundAuth: ExtractBackgroundAuth
) : BcGatewayProxy {

    @Value("\${app.bc-gateway.url}")
    private lateinit var baseUrl: String

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
            .awaitFirst()
    }

    override suspend fun getWithdrawData(symbol: String, network: String): WithdrawData {
        return webClient.get()
            .uri("$baseUrl/currency/$symbol/network/$network/withdrawData")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<WithdrawData>()
            .awaitFirst()
    }
}
