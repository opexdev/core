package co.nilin.opex.bcgateway.ports.authproxy.impl

import co.nilin.opex.bcgateway.core.model.otc.LoginRequest
import co.nilin.opex.bcgateway.core.model.otc.LoginResponse
import co.nilin.opex.bcgateway.core.spi.AuthProxy
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import java.net.URI

inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

@Component
class AuthProxyImpl(private val webClient: WebClient) : AuthProxy {


    @Value("\${app.auth.url}")
    private lateinit var baseUrl: String

    override suspend fun getToken(loginRequest: LoginRequest): LoginResponse {
        return webClient.post()
            .uri(URI.create("${baseUrl}/api/v1/login"))
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .body(
                BodyInserters.fromFormData("mobile", loginRequest.clientId)
                    .with("password", loginRequest.clientSecret)
            )
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono(typeRef<LoginResponse>())
            .awaitFirst()
    }

}
