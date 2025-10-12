package co.nilin.opex.wallet.ports.otp.impl

import co.nilin.opex.wallet.core.inout.otp.NewOTPRequest
import co.nilin.opex.wallet.core.inout.otp.OTPVerifyResponse
import co.nilin.opex.wallet.core.inout.otp.TempOtpResponse
import co.nilin.opex.wallet.core.inout.otp.VerifyOTPRequest
import co.nilin.opex.wallet.core.spi.OtpProxy
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class OtpProxyImpl(private val webClient: WebClient) : OtpProxy {

    @Value("\${app.otp.url}")
    private lateinit var baseUrl: String

    override suspend fun requestOTP(newOTPRequest: NewOTPRequest): TempOtpResponse {
        return webClient.post().uri("$baseUrl/otp")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(newOTPRequest))
            .retrieve()
            .awaitBody()
    }

    override suspend fun verifyOTP(verifyOTPRequest: VerifyOTPRequest): OTPVerifyResponse {
        val request = object {
            val userId = verifyOTPRequest.userId
            val otpCodes = verifyOTPRequest.otpCodes.map {
                object {
                    val type = it.type
                    val code = it.code
                }
            }
        }
        return webClient.post().uri("$baseUrl/otp/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .awaitBody()
    }
}
