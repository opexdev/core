package co.nilin.opex.otp.app.proxy

import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriComponentsBuilder

@Component
class KaveNegarProxy(
    @Value("\${otp.sms.provider.api-key}")
    private val apiKey: String,
    private val webClient: WebClient
) {

    private val logger by LoggerDelegate()
    private val baseUrl = "https://api.kavenegar.com/v1/$apiKey/"

    suspend fun send(receiver: String, message: String, sender: String? = null, type: String? = null) {
        val uri = UriComponentsBuilder.fromUriString("$baseUrl/sms/send.json")
            .queryParam("receptor", receiver)
            .queryParam("message", message)
            .queryParam("sender", sender)
            .queryParam("type", type)
            .build().toUri()

        try {
            val response = webClient.get()
                .uri(uri)
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono<String>()
                .awaitSingleOrNull()
            logger.info("Message sent to receiver $receiver.\n$response")
        } catch (e: Exception) {
            logger.error("Failed to send SMS", e)
        }
    }
}