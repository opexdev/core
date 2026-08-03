package co.nilin.opex.otp.app.proxy

import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.LoggerDelegate
import co.nilin.opex.otp.app.data.SMSProviderType
import co.nilin.opex.otp.app.repository.SMSProviderRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriComponentsBuilder

@Component
class SMSIRProxy(
    private val webClient: WebClient,
    private val smsProviderRepository: SMSProviderRepository,
) : SMSProvider {

    override val type = SMSProviderType.SMSIR


    private val logger by LoggerDelegate()

    override suspend fun send(receiver: String, message: String): Boolean {
        val config = smsProviderRepository.findById(type.name)
            ?: throw OpexError.UnableToSendOTP.exception()

        val uri = UriComponentsBuilder
            .fromUriString("${config.baseUrl}/v1/send")
            .queryParam("username", config.username)
            .queryParam("password", config.password)
            .queryParam("mobile", receiver)
            .queryParam("line", config.sender)
            .queryParam("text", "otp code : $message")
            .build(true)
            .toUri()

        return try {
            val response = webClient.get()
                .uri(uri)
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .onStatus({ it.isError }) { it.createException() }
                .bodyToMono<String>()
                .awaitSingleOrNull()

            logger.debug("Message sent to receiver $receiver. Response: $response")
            true
        } catch (e: Exception) {
            logger.error("Failed to send SMS", e)
            false
        }
    }
}