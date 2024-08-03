package co.nilin.opex.bcgateway.omniwallet.proxy

import co.nilin.opex.bcgateway.core.model.OmniBalance
import co.nilin.opex.bcgateway.core.spi.OmniWalletManager
import co.nilin.opex.bcgateway.omniwallet.model.AddressBalanceWithUsd
import co.nilin.opex.bcgateway.omniwallet.model.ChainBalanceResponse
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.net.URI

inline fun <reified T : Any?> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}
data class TotalAssetByChainWithUsd(val balance: BigDecimal, val chain: String? = null, val symbol: String? = null, val balanceUsd: BigDecimal? = null)

@Component
class OmniWalletProxy(private val webClient: WebClient) {


    @Value("\${app.omni-wallet.url}")
    private lateinit var baseUrl: String

    private val logger: Logger = LoggerFactory.getLogger(OmniWalletProxy::class.java)

    suspend fun getAssetBalance(network: String): TotalAssetByChainWithUsd? {
//        return TotalAssetByChainWithUsd(BigDecimal(15),network,"", BigDecimal(65))
//
        logger.info("----&&&&&&&&&&&----")

        return webClient.get()
                .uri("${baseUrl}/v1/balance/chain/${network}/total")
                {
                    it.queryParam("excludeZero", false)
                    it.build()
                }
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(typeRef<TotalAssetByChainWithUsd?>())
                .doOnError { e -> logger.info("An error happened during get balance of chain $network :  ${e.message}") }
                .onErrorReturn(TotalAssetByChainWithUsd(balance = BigDecimal.ZERO))
                .log()
                .awaitFirst()
    }

    suspend fun getTokenBalance(tokenAddress: String, network: String): List<AddressBalanceWithUsd>? {
//        return listOf( AddressBalanceWithUsd("", BigDecimal.TEN, BigDecimal.TEN))
        return webClient.get()
                .uri("${baseUrl}/v1/balance/token/address/${tokenAddress}")
                {
                    it.queryParam("excludeZero", false)
                    it.build()
                }
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(typeRef<List<AddressBalanceWithUsd>?>())
                .doOnError { e -> logger.info("An error happened during get balance of token $tokenAddress :  ${e.message}") }
                .onErrorReturn(listOf(AddressBalanceWithUsd(tokenAddress, BigDecimal.ZERO, BigDecimal.ZERO)))
                .log()
                .awaitFirst()

    }
}
