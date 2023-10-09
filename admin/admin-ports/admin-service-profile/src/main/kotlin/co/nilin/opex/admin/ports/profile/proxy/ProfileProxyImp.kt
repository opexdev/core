package co.nilin.opex.admin.ports.profile.proxy

import co.nilin.opex.admin.core.data.ProfileRequest
import co.nilin.opex.admin.core.data.ProfileResponse
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactor.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.*

@Component
class ProfileProxyImp(@Qualifier("logRequest")private val webClient: WebClient) {
    private val logger = LoggerFactory.getLogger(ProfileProxyImp::class.java)

    @Value("\${app.profile.url}")
    private lateinit var baseUrl: String


    suspend fun getProfile(profileRequest: ProfileRequest): Flow<ProfileResponse> {
        val headers = HttpHeaders()
        headers[HttpHeaders.CONTENT_TYPE] = MediaType.APPLICATION_JSON_VALUE
        headers.setBearerAuth((ReactiveSecurityContextHolder.getContext()?.awaitSingleOrNull()?.authentication as JwtAuthenticationToken).token.tokenValue)

        return webClient.post()
                .uri(baseUrl)
                .accept(MediaType.APPLICATION_JSON)
                .headers { httpHeaders ->
                    httpHeaders.addAll(headers)
                }
                .body(BodyInserters.fromValue(profileRequest))
                .retrieve()
                .onStatus({ t -> t.isError }) {
                    it.createException()
                }
                .bodyToFlow<ProfileResponse>()


    }

    fun authHeader(): ExchangeFilterFunction {
        val oauthToken = ReactiveSecurityContextHolder.getContext()?.block()?.authentication as JwtAuthenticationToken?
        return ExchangeFilterFunction { request: ClientRequest?, next: ExchangeFunction ->
            next.exchange(ClientRequest.from(request!!).headers { headers: HttpHeaders -> headers.setBearerAuth(oauthToken?.token?.tokenValue!!) }.build())
        }
    }
}