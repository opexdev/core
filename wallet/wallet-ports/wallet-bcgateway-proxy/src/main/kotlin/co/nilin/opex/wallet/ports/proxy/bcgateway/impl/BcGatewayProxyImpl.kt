package co.nilin.opex.wallet.ports.proxy.bcgateway.impl

import co.nilin.opex.wallet.core.inout.CryptoCurrencyCommand
import co.nilin.opex.wallet.core.inout.CryptoImps
import co.nilin.opex.wallet.core.spi.BcGatewayProxy
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.net.URI


inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

@Component
class BcGatewayProxyImpl(private val webClient: WebClient) : BcGatewayProxy {

    @Value("\${app.bc-gateway.url}")
    private lateinit var baseUrl: String

    private val logger = LoggerFactory.getLogger(BcGatewayProxyImpl::class.java)

    override suspend fun createNewCurrency(currencyImpl: CryptoCurrencyCommand, internalToken: String?): CryptoImps? {
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
                .bodyToMono(typeRef<CryptoImps>())
                .log()
                .awaitFirst()
    }

    override suspend fun updateImplOfCryptoCurrency(currencyImp: CryptoCurrencyCommand, internalToken: String?): CryptoImps? {

        return webClient.put()
                .uri(URI.create("$baseUrl/crypto-currency/impl/${currencyImp.implUuid}"))
                .headers { httpHeaders ->
                    run {
                        httpHeaders.add("Content-Type", "application/json");
                        internalToken?.let { httpHeaders.add("Authorization", "Bearer $it") }
                    }
                }
                .bodyValue(currencyImp)
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono(typeRef<CryptoImps>())
                .log()
                .awaitFirst()
    }

    override suspend fun fetchImpls(currencySymbol: String?, internalToken: String?): CryptoImps? {

        return webClient.get()
                .uri(URI.create("$baseUrl/crypto-currency/impls"))
                .headers { httpHeaders ->
                    run {
                        httpHeaders.add("Content-Type", "application/json");
                        internalToken?.let { httpHeaders.add("Authorization", "Bearer $it") }
                    }
                }
                .attributes {
                    currencySymbol?.let {
                        val attributeMap: MutableMap<String, String> = HashMap()
                        attributeMap["currency"] = it
                    }
                }
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono(typeRef<CryptoImps>())
                .log()
                .awaitFirst()
    }

    override suspend fun fetchImplDetail(implUuid: String, internalToken: String?): CryptoCurrencyCommand? {
        return webClient.get()
                .uri(URI.create("$baseUrl/crypto-currency/impl/${implUuid}"))
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
}
