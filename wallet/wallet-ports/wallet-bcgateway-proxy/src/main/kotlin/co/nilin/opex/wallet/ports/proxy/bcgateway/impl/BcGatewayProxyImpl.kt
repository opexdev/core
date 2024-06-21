package co.nilin.opex.wallet.ports.proxy.bcgateway.impl

import co.nilin.opex.wallet.core.inout.CryptoCurrencyCommand
import co.nilin.opex.wallet.core.inout.CryptoImps
import co.nilin.opex.wallet.core.spi.BcGatewayProxy
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.asm.TypeReference
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodyOrNull
import org.springframework.web.reactive.function.client.bodyToMono
import java.net.URI


inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

@Component
class BcGatewayProxyImpl(private val webClient: WebClient) : BcGatewayProxy {

    @Value("\${app.bc-gateway.url}")
    private lateinit var baseUrl: String

    private val logger = LoggerFactory.getLogger(BcGatewayProxyImpl::class.java)

    override suspend fun createImpl(currencyImpl: CryptoCurrencyCommand, internalToken: String?): CryptoCurrencyCommand? {
        return webClient.post()
                .uri(URI.create("$baseUrl/crypto-currency/${currencyImpl.currencySymbol}/impl"))

                .headers { httpHeaders ->
                    run {
                        httpHeaders.add("Content-Type", "application/json");
                        internalToken?.let { httpHeaders.add("Authorization", "Bearer $it") }
                    }
                }
                .bodyValue(currencyImpl)
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono(typeRef<CryptoCurrencyCommand>())
                .log()
                .awaitFirst()
    }

    override suspend fun updateImpl(currencyImp: CryptoCurrencyCommand, internalToken: String?): CryptoCurrencyCommand? {
        return webClient.put()
                .uri(URI.create("$baseUrl/crypto-currency/${currencyImp.currencySymbol}/impl/${currencyImp.implUuid}"))
                .headers { httpHeaders ->
                    run {
                        httpHeaders.add("Content-Type", "application/json");
                        internalToken?.let { httpHeaders.add("Authorization", "Bearer $it") }
                    }
                }
                .bodyValue(currencyImp)
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono(typeRef<CryptoCurrencyCommand>())
                .log()
                .awaitFirst()
    }

    override suspend fun fetchImpls(currencySymbol: String?, internalToken: String?): CryptoImps? {

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
                    .bodyToMono(typeRef<CryptoImps>())
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
                    .bodyToMono(typeRef<CryptoImps>())
                    .log()
                    .awaitFirst()
    }

    override suspend fun fetchImplDetail(implUuid: String, currencySymbol: String, internalToken: String?): CryptoCurrencyCommand? {
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
                .bodyToMono(typeRef<CryptoCurrencyCommand>())
                .log()
                .awaitFirst()
    }

    override suspend fun deleteImpl(implUuid: String, currencySymbol: String, internalToken: String?) {
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
