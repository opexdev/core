package co.nilin.opex.wallet.ports.proxy.accountant.impl

import co.nilin.opex.wallet.core.spi.AccountantProxy
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.math.BigDecimal
import java.net.URI

@Component
class AccountantProxyImpl(private val webClient: WebClient) : AccountantProxy {

    @Value("\${app.accountant.url}")
    private lateinit var baseUrl: String

    override suspend fun canRequestWithdraw(
        uuid: String,
        userLevel: String,
        currency: String,
        amount: BigDecimal
    ): Boolean {
        return webClient.get()
            .uri(URI.create("$baseUrl/$uuid/$userLevel/request_withdraw/${amount}_$currency/allowed"))
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono(Boolean::class.java)
            .awaitFirst()
    }
}
