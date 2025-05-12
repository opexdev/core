package co.nilin.opex.auth.proxy

import co.nilin.opex.auth.model.OTPReceiver
import co.nilin.opex.auth.model.OTPVerifyRequest
import co.nilin.opex.auth.model.OTPVerifyResponse
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.toEntity

@Component
class OTPProxy(@Qualifier("otpWebClient") private val webClient: WebClient) {

    suspend fun requestOTP(userId: String, receivers: List<OTPReceiver>) {
        val request = object {
            val userId = userId
            val receivers = receivers
        }

        webClient.post().uri("/otp")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .toBodilessEntity()
            .awaitSingleOrNull()
    }

    suspend fun verifyOTP(verifyRequest: OTPVerifyRequest): Boolean {
        val request = object {
            val userId = verifyRequest.userId
            val otpCodes = verifyRequest.otpCodes.map {
                object {
                    val type = it.otpType
                    val code = it.code
                }
            }
        }
        val response = webClient.post().uri("/otp/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .toEntity<OTPVerifyResponse>()
            .awaitSingle()

        if (response.statusCode.isError) {
            return false
        }

        return response.body?.result ?: false
    }
}