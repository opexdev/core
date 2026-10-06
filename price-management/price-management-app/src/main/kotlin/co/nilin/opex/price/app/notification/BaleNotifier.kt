package co.nilin.opex.price.app.notification

import co.nilin.opex.price.core.spi.Notifier
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class BaleNotifier(
    @Value("\${app.bale.enabled:true}") private val enabled: Boolean,
    @Value("\${app.bale.base-url:https://tapi.bale.ai}") baseUrl: String,
    @Value("\${app.bale.bot-token:}") private val botToken: String,
    @Value("\${app.bale.chat-id:}") private val chatId: String,
) : Notifier {

    private val logger = LoggerFactory.getLogger(BaleNotifier::class.java)
    private val webClient = WebClient.create(baseUrl)

    override suspend fun notify(message: String) {
        if (!enabled) {
            logger.debug("Bale notifier is disabled; skipping alert: {}", message)
            return
        }
        if (botToken.isBlank() || chatId.isBlank()) {
            logger.warn("Bale notifier is not configured; skipping alert")
            return
        }
        runCatching {
            webClient.post()
                .uri("/bot{token}/sendMessage", botToken)
                .bodyValue(mapOf("chat_id" to chatId, "text" to message))
                .retrieve()
                .toBodilessEntity()
                .awaitFirstOrNull()
        }.onFailure { logger.error("Failed to send Bale alert: ${it.message}", it) }
    }
}
