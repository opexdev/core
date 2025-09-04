package co.nilin.opex.common.translation

import co.nilin.opex.common.config.CommonWebClient
import co.nilin.opex.common.data.MessageTranslation
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

inline fun <reified T : Any?> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

@Component
class ConfigClient(@Qualifier("CommonWebClient") private val webClient: CommonWebClient
) {
    suspend fun getMessagesUpdatedAfter(lastUpdate: Long?): List<MessageTranslation>? {
        return webClient.delegate.get().uri("http://opex-config:8080/messages") {
            it.queryParam("last-update", lastUpdate)
            it.build()
        }.retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono(typeRef<List<MessageTranslation>>())
                .log()
                .awaitFirst()
    }
}
