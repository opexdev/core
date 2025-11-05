package co.nilin.opex.common.translation

import co.nilin.opex.common.config.CommonWebClient
import co.nilin.opex.common.data.MessageTranslation
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component

inline fun <reified T : Any?> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

@Component
@ConditionalOnProperty(name = ["app.custom-message.enabled"], havingValue = "true", matchIfMissing = false)
class ConfigClient(
        @Qualifier("CommonWebClient") private val webClient: CommonWebClient
) {
    @Value("\${app.custom-message.base-url}")
    private lateinit var customMessageBaseUrl: String
    suspend fun getMessagesUpdatedAfter(lastUpdate: Long?): List<MessageTranslation>? {
        return webClient.delegate.get().uri(customMessageBaseUrl) {
            it.queryParam("last-update", lastUpdate)
            it.build()
        }.retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono(typeRef<List<MessageTranslation>>())
                .log()
                .awaitFirst()
    }
}
