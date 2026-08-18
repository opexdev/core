package co.nilin.opex.auth.proxy

import co.nilin.opex.auth.model.*
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class OTPProxy(@Qualifier("otpWebClient") private val webClient: WebClient) {

    suspend fun requestOTP(
        userId: String,
        receivers: List<OTPReceiver>,
        otpAction: OTPAction? = null
    ): TempOtpResponse {
        val request = object {
            val userId = userId
            val receivers = receivers
            val action = otpAction
        }

        return webClient.post().uri("/otp")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .awaitBody()
    }

    suspend fun verifyOTP(verifyRequest: OTPVerifyRequest): OTPVerifyResponse {
        val request = object {
            val userId = verifyRequest.userId
            val otpCodes = verifyRequest.otpCodes.map {
                object {
                    val type = it.otpType
                    val code = it.code
                }
            }
        }
        return webClient.post().uri("/otp/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .awaitBody()
    }

    // ---------------- TOTP ----------------

    suspend fun setupTOTP(userId: String, label: String): SetupTOTPResponse {
        return webClient.post()
            .uri("/totp/setup")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(SetupTOTPRequest(userId, label))
            .retrieve()
            .awaitBody()
    }

    suspend fun verifyTOTPSetup(userId: String, code: String) {
        webClient.post()
            .uri("/totp/setup/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(VerifyTOTPRequest(userId, code))
            .retrieve()
            .toBodilessEntity()
            .awaitSingle()
    }

    suspend fun verifyTOTP(userId: String, code: String): VerifyTOTPResponse {
        return webClient.post()
            .uri("/totp/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(VerifyTOTPRequest(userId, code))
            .retrieve()
            .awaitBody()
    }

    suspend fun queryTOTP(userId: String): TOTPQueryResponse {
        return webClient.get()
            .uri("/totp/query/$userId")
            .retrieve()
            .awaitBody()
    }


}