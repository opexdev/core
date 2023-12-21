package co.nilin.opex.wallet.ports.proxy.bcgateway.impl

import co.nilin.opex.wallet.core.model.CurrencyImp
import co.nilin.opex.wallet.core.model.PropagateCurrencyChanges
import co.nilin.opex.wallet.core.spi.BcGatewayProxy
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.net.URI

inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

@Component
class WalletProxyImpl(@Qualifier("otcWebClient") private val webClient: WebClient) : BcGatewayProxy {

    @Value("\${app.bc-gateway.url}")
    private lateinit var baseUrl: String



    override suspend fun createCurrency(currencyImp: PropagateCurrencyChanges) {
        webClient.post()
            .uri(URI.create("$baseUrl/currency/${currencyImp.currencySymbol}"))
            .header("Content-Type", "application/json")
            .bodyValue(currencyImp)
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono(typeRef<CurrencyImp>())
            .log()
            .awaitFirst()
    }

    override suspend fun updateCurrency() {
        TODO("Not yet implemented")
    }

    override suspend fun getCurrencyInfo() {
        TODO("Not yet implemented")
    }
}
