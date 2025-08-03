package co.nilin.opex.profile.ports.inquiry.utils

import co.nilin.opex.profile.ports.inquiry.data.TokenResponse
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration
import java.time.Instant

@Service
class TokenProvider(
    @Qualifier("plainWebClient") private val webClient: WebClient,
    @Value("\${inquiry.url}") private var baseUrl: String,
    @Value("\${inquiry.api-key}") private var apiKey: String,
    @Value("\${inquiry.secret-key}") private var secretKey: String,
) {

    @Volatile
    private var cachedToken: String? = null

    @Volatile
    private var expiryTime: Instant? = null

    private val tokenLock = Mutex()

    suspend fun getToken(): String {

        val now = Instant.now()
        if (cachedToken != null && expiryTime != null && now.isBefore(expiryTime)) {
            return cachedToken!!
        }
        tokenLock.withLock {
            if (cachedToken == null || expiryTime == null || now.isAfter(expiryTime)) {
                val response =
                    webClient.post()
                        .uri("$baseUrl/v1/tokens/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(mapOf("apiKey" to apiKey, "secretKey" to secretKey))
                        .retrieve()
                        .bodyToMono(TokenResponse::class.java)
                        .awaitSingle()

                cachedToken = response.accessToken
                expiryTime = now.plus(Duration.ofHours(23))
            }

            return cachedToken!!
        }
    }
}