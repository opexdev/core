package co.nilin.opex.common.proxy

import co.nilin.opex.common.config.CommonWebClient
import co.nilin.opex.common.data.MessageTranslation
import co.nilin.opex.common.utils.typeRef
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component


@Component
@ConditionalOnProperty(name = ["app.custom-message.enabled"], havingValue = "true", matchIfMissing = false)
class CustomMessageClient(
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
