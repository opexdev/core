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

@Component
class OmniWalletProxy(private val webClient: WebClient){


//    @Value("\${app.omni-wallet.url}")
//    private lateinit var baseUrl: String

     suspend fun getAssetBalance( network: String): ChainBalanceResponse {
         return  ChainBalanceResponse(listOf( AddressBalanceWithUsd("2345", BigDecimal(15),BigDecimal(65))))

//        return ChainBalanceResponse(data=webClient.get()
////                .uri(URI.create("${baseUrl}/{chainId}"))
////                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
////                .retrieve()
////                .onStatus({ t -> t.isError }, {
////                    it.createException() })
////                .bodyToMono(typeRef<List<AddressBalanceWithUsd>>())
////                .log()
////                .awaitFirst())

        }

     suspend fun getTokenBalance(tokenAddress: String,network: String): OmniBalance {
        return OmniBalance("ETH", "ethereum", BigDecimal.TEN)
//                 return ChainBalanceResponse(data=webClient.get()
//                .uri(URI.create("${baseUrl}/{chainId}"))
//                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
//                .retrieve()
//                .onStatus({ t -> t.isError }, {
//                    it.createException() })
//                .bodyToMono(typeRef<List<AddressBalanceWithUsd>>())
//                .log()
//                .awaitFirst())


    }
}