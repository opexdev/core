package co.nilin.opex.wallet.ports.proxy.bcgateway.impl

import co.nilin.opex.wallet.core.inout.OnChainGatewayCommand
import co.nilin.opex.wallet.core.inout.CurrencyGatewayCommand
import co.nilin.opex.wallet.core.inout.CurrencyGateways
import co.nilin.opex.wallet.core.spi.BcGatewayProxy
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.net.URI


inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

@Component
class BcGatewayProxyGateway(private val webClient: WebClient) : BcGatewayProxy {

    @Value("\${app.bc-gateway.url}")
    private lateinit var baseUrl: String

    private val logger = LoggerFactory.getLogger(BcGatewayProxyGateway::class.java)

    override suspend fun createGateway(currencyGateway: CurrencyGatewayCommand, internalToken: String?): CurrencyGatewayCommand? {
        return webClient.post()
                .uri(URI.create("$baseUrl/crypto-currency/${currencyGateway.currencySymbol}/impl"))

                .headers { httpHeaders ->
                    run {
                        httpHeaders.add("Content-Type", "application/json");
                        internalToken?.let { httpHeaders.add("Authorization", "Bearer $it") }
                    }
                }
                .bodyValue(currencyGateway)
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono(typeRef<OnChainGatewayCommand>())
                .log()
                .awaitFirst()
    }

    override suspend fun updateGateway(currencyImp: CurrencyGatewayCommand, internalToken: String?): CurrencyGatewayCommand? {
        return webClient.put()
                .uri(URI.create("$baseUrl/crypto-currency/${currencyImp.currencySymbol}/impl/${currencyImp.gatewayUuid}"))
                .headers { httpHeaders ->
                    run {
                        httpHeaders.add("Content-Type", "application/json");
                        internalToken?.let { httpHeaders.add("Authorization", "Bearer $it") }
                    }
                }
                .bodyValue(currencyImp)
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono(typeRef<OnChainGatewayCommand>())
                .log()
                .awaitFirst()
    }

    override suspend fun fetchGateways(currencySymbol: String?, internalToken: String?): CurrencyGateways? {

        if (currencySymbol == null)
            return webClient.get()
                    .uri(URI.create("$baseUrl/crypto-currency/impls"))
                    .headers { httpHeaders ->
                        run {
                            httpHeaders.add("Content-Type", "application/json");
                            internalToken?.let { httpHeaders.add("Authorization", "Bearer $it") }
                        }
                    }
                    .retrieve()
                    .onStatus({ t -> t.isError }, { it.createException() })
                    .bodyToMono(typeRef<CurrencyGateways>())
                    .log()
                    .awaitFirst()
        else
            return webClient.get()
                    .uri(URI.create("$baseUrl/crypto-currency/${currencySymbol}/impls"))
                    .headers { httpHeaders ->
                        run {
                            httpHeaders.add("Content-Type", "application/json");
                            internalToken?.let { httpHeaders.add("Authorization", "Bearer $it") }
                        }
                    }
                    .retrieve()
                    .onStatus({ t -> t.isError }, { it.createException() })
                    .bodyToMono(typeRef<CurrencyGateways>())
                    .log()
                    .awaitFirst()
    }

    override suspend fun fetchGatewayDetail(implUuid: String, currencySymbol: String, internalToken: String?): OnChainGatewayCommand? {
        return webClient.get()
                .uri(URI.create("$baseUrl/crypto-currency/${currencySymbol}/impl/${implUuid}"))
                .headers { httpHeaders ->
                    run {
                        httpHeaders.add("Content-Type", "application/json");
                        internalToken?.let { httpHeaders.add("Authorization", "Bearer $it") }
                    }
                }
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono(typeRef<OnChainGatewayCommand>())
                .log()
                .awaitFirst()
    }

    override suspend fun deleteGateway(implUuid: String, currencySymbol: String, internalToken: String?) {
        webClient.delete()
                .uri(URI.create("$baseUrl/crypto-currency/${currencySymbol}/impl/${implUuid}"))
                .headers { httpHeaders ->
                    run {
                        httpHeaders.add("Content-Type", "application/json");
                        internalToken?.let { httpHeaders.add("Authorization", "Bearer $it") }
                    }
                }
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono(typeRef<Void>())
                .log()
                .awaitFirstOrNull()


    }
}
