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
    @Value("\${otp.sms.provider.url}")
    private val url: String,
    @Value("\${otp.sms.provider.api-key}")
    private val apiKey: String,
    @Value("\${otp.sms.provider.template}")
    private val template: String,
    private val webClient: WebClient
) {

    private val logger by LoggerDelegate()
    private val baseUrl = "${url}/$apiKey/"

    suspend fun send(receiver: String, message: String, sender: String? = null, type: String? = null): Boolean {
        val uri = UriComponentsBuilder.fromUriString("$baseUrl/verify/lookup.json")
            .queryParam("receptor", receiver)
            .queryParam("template", template)
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