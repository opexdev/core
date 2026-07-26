package co.nilin.opex.otp.app.proxy

import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.LoggerDelegate
import co.nilin.opex.otp.app.data.SMSProviderType
import co.nilin.opex.otp.app.repository.SMSProviderRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriComponentsBuilder

@Component
class KaveNegarProxy(
    private val webClient: WebClient,
    private val smsProviderRepository: SMSProviderRepository,
) : SMSProvider {

    override val type = SMSProviderType.KAVENEGAR


    private val logger by LoggerDelegate()

    override suspend fun send(receiver: String, message: String): Boolean {
        val config = smsProviderRepository.findById(type.name) ?: throw OpexError.UnableToSendOTP.exception()
        val baseUrl = "${config.baseUrl}/${config.apiKey}/"

        val uri = UriComponentsBuilder.fromUriString("$baseUrl/verify/lookup.json")
            .queryParam("receptor", receiver)
            .queryParam("template", config.template)
            .queryParam("token", message)
            .build().toUri()

        return try {
            val response = webClient.get()
                .uri(uri)
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono<String>()
                .awaitSingleOrNull()
            logger.debug("Message sent to receiver $receiver.\n$response")
            true
        } catch (e: Exception) {
            logger.error("Failed to send SMS", e)
            false
        }
    }
}