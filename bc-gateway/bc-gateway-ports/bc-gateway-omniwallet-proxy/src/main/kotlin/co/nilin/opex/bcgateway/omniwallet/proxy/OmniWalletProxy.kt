package co.nilin.opex.bcgateway.omniwallet.proxy

import co.nilin.opex.bcgateway.core.model.OmniBalance
import co.nilin.opex.bcgateway.core.spi.OmniWalletManager
import co.nilin.opex.bcgateway.omniwallet.model.AddressBalanceWithUsd
import co.nilin.opex.bcgateway.omniwallet.model.ChainBalanceResponse
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.math.BigDecimal
import java.net.URI

inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}
data class TotalAssetByChainWithUsd( val balance: BigDecimal,val chain: String?=null, val symbol: String?=null, val balanceUsd: BigDecimal?=null)

    @Component
    class OmniWalletProxy(private val webClient: WebClient) {


        @Value("\${app.omni-wallet.url}")
        private lateinit var baseUrl: String

        suspend fun getAssetBalance(network: String): TotalAssetByChainWithUsd {
//        return ChainBalanceResponse(listOf(AddressBalanceWithUsd("2345", BigDecimal(15), BigDecimal(65))))

            return webClient.get()
                    .uri(URI.create("${baseUrl}/chain/${network}/total"))
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .onStatus({ t -> t.isError }, {
                        it.createException()
                    })
                    .bodyToMono(typeRef<TotalAssetByChainWithUsd>())
                    .log()
                    .doOnError { TotalAssetByChainWithUsd(balance = BigDecimal.ZERO) }
                    .awaitFirst()

        }

        suspend fun getTokenBalance(tokenAddress: String, network: String): List<AddressBalanceWithUsd> {
//        return OmniBalance("ETH", "ethereum", BigDecimal.TEN)
            return webClient.get()
                    .uri(URI.create("${baseUrl}/token/address/${tokenAddress}"))
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .onStatus({ t -> t.isError }, {
                        it.createException()
                    })
                    .bodyToMono(typeRef<List<AddressBalanceWithUsd>>())
                    .doOnError { listOf(AddressBalanceWithUsd(tokenAddress, BigDecimal.ZERO, BigDecimal.ZERO)) }
                    .log()
                    .awaitFirst()


        }
    }
