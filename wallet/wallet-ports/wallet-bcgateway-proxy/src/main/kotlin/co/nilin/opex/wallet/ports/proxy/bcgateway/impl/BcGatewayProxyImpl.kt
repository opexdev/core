package co.nilin.opex.wallet.ports.proxy.bcgateway.impl

import co.nilin.opex.wallet.core.inout.OnChainGatewayCommand
import co.nilin.opex.wallet.core.inout.CurrencyGatewayCommand

import co.nilin.opex.wallet.core.spi.GatewayPersister
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.net.URI


inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

@Component("onChainGateway")
class OnChainGatewayProxyGateway(private val webClient: WebClient) : GatewayPersister {

    @Value("\${app.bc-gateway.url}")
    private lateinit var baseUrl: String

    private val logger = LoggerFactory.getLogger(OnChainGatewayProxyGateway::class.java)

    override suspend fun createGateway(currencyGateway: CurrencyGatewayCommand, internalToken: String?): CurrencyGatewayCommand? {
        return webClient.post()
                .uri(URI.create("$baseUrl/crypto-currency/${currencyGateway.currencySymbol}/gateway"))

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
                .uri(URI.create("$baseUrl/crypto-currency/${currencyImp.currencySymbol}/gateway/${currencyImp.gatewayUuid}"))
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

    override suspend fun fetchGateways(currencySymbol: String?, internalToken: String?): List<CurrencyGatewayCommand>? {

        if (currencySymbol == null)
            return webClient.get()
                    .uri(URI.create("$baseUrl/crypto-currency/gateways"))
                    .headers { httpHeaders ->
                        run {
                            httpHeaders.add("Content-Type", "application/json");
                            internalToken?.let { httpHeaders.add("Authorization", "Bearer $it") }
                        }
                    }
                    .retrieve()
                    .onStatus({ t -> t.isError }, { it.createException() })
                    .bodyToMono(typeRef<List<OnChainGatewayCommand>>())
                    .log()
                    .awaitFirst()
        else
            return webClient.get()
                    .uri(URI.create("$baseUrl/crypto-currency/${currencySymbol}/gateways"))
                    .headers { httpHeaders ->
                        run {
                            httpHeaders.add("Content-Type", "application/json");
                            internalToken?.let { httpHeaders.add("Authorization", "Bearer $it") }
                        }
                    }
                    .retrieve()
                    .onStatus({ t -> t.isError }, { it.createException() })
                    .bodyToMono(typeRef<List<OnChainGatewayCommand>>())
                    .log()
                    .awaitFirst()
    }

    override suspend fun fetchGatewayDetail(implUuid: String, currencySymbol: String, internalToken: String?): OnChainGatewayCommand? {
        return webClient.get()
                .uri(URI.create("$baseUrl/crypto-currency/${currencySymbol}/gateway/${implUuid}"))
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
                .uri(URI.create("$baseUrl/crypto-currency/${currencySymbol}/gateway/${implUuid}"))
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
