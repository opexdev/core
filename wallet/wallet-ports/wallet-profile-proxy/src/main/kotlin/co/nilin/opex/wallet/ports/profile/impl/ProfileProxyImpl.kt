package co.nilin.opex.wallet.ports.profile.impl

import co.nilin.opex.wallet.core.inout.profile.Profile
import co.nilin.opex.wallet.core.spi.ProfileProxy
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class ProfileProxyImpl(private val webClient: WebClient) : ProfileProxy {

    @Value("\${app.profile.url}")
    private lateinit var baseUrl: String

    override suspend fun getProfile(token: String): Profile {
        return webClient.get()
            .uri("$baseUrl/personal-data")
            .headers { it.setBearerAuth(token) }
            .retrieve()
            .awaitBody()
    }
}
