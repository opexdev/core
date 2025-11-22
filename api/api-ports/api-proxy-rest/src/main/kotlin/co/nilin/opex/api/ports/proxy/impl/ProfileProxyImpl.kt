package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.Profile
import co.nilin.opex.api.core.spi.ProfileProxy
import co.nilin.opex.api.ports.proxy.config.ProxyDispatchers
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class ProfileProxyImpl(@Qualifier("generalWebClient") private val webClient: WebClient) : ProfileProxy {

    @Value("\${app.profile.url}")
    private lateinit var baseUrl: String

    override suspend fun getProfile(
        uuid: String,
        token: String
    ): Profile? {
        return withContext(ProxyDispatchers.wallet) {
            webClient.get()
                .uri("$baseUrl/admin/profile/${uuid}")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono<Profile>()
                .awaitSingleOrNull()
        }
    }
}