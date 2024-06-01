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

    override suspend fun createNewCurrency(currencyImp: CryptoCurrencyCommand, internalToken: String?): CryptoImps? {
        return webClient.post()
                .uri(URI.create("$baseUrl/currency/${currencyImp.currencyUUID}"))

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

    override suspend fun updateImpOfCryptoCurrency(currencyImp: CryptoCurrencyCommand,internalToken: String?): CryptoImps? {

        return webClient.put()
                .uri(URI.create("$baseUrl/currency/${currencyImp.currencyUUID}"))
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

    override suspend fun fetchImpsOfCryptoCurrency(currencyUuid: String,internalToken:String?): CryptoImps? {
        return webClient.delete()
                .uri(URI.create("$baseUrl/currency/${currencyUuid}"))
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
                .awaitFirst()    }
}
