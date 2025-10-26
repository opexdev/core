package co.nilin.opex.wallet.ports.proxy.profile.impl

import co.nilin.opex.wallet.core.spi.ProfileProxy
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.net.URI

@Component
class ProfileProxyImpl(private val webClient: WebClient) : ProfileProxy {

    @Value("\${app.profile.url}")
    private lateinit var baseUrl: String

    override suspend fun verifyBankAccountOwnership(
        token: String,
        cardNumber: String?,
        iban: String?
    ): Boolean {
        val params = buildString {
            val query = listOfNotNull(
                cardNumber?.takeIf { it.isNotBlank() }?.let { "cardNumber=$it" },
                iban?.takeIf { it.isNotBlank() }?.let { "iban=$it" }
            ).joinToString("&")

            append("$baseUrl/bank-account/ownership")
            if (query.isNotEmpty()) append("?$query")
        }

        return webClient.get()
            .uri(URI.create(params))
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono(Boolean::class.java)
            .awaitFirst()
    }


}
