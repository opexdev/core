package co.nilin.opex.wallet.app.notification

import co.nilin.opex.wallet.core.spi.Notifier
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class MessengerNotifier(
    @Value("\${app.notifier.enabled:false}") private val enabled: Boolean,
    @Value("\${app.notifier.base-url:}") private val baseUrl: String,
    @Value("\${app.notifier.bot-token:}") private val botToken: String,
    @Value("\${app.notifier.chat-id:}") private val chatId: String,
) : Notifier {

    private val logger = LoggerFactory.getLogger(MessengerNotifier::class.java)
    private val webClient = WebClient.create(baseUrl)

    override suspend fun notify(message: String): Boolean {
        // True without delivering: being switched off or unconfigured is not a transient failure,
        // and reporting it as one would make the caller retry on every run forever
        if (!enabled) {
            logger.debug("Notifier is disabled; skipping alert: {}", message)
            return true
        }
        if (baseUrl.isBlank() || botToken.isBlank() || chatId.isBlank()) {
            logger.warn("Notifier is not configured; skipping alert")
            return true
        }
        return runCatching {
            webClient.post()
                .uri("/bot{token}/sendMessage", botToken)
                .bodyValue(mapOf("chat_id" to chatId, "text" to message))
                .retrieve()
                .toBodilessEntity()
                .awaitFirstOrNull()
        }.onFailure { logger.error("Failed to send alert ({})", it.javaClass.simpleName) }
            .isSuccess
    }
}
