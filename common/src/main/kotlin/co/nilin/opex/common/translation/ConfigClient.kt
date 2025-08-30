package co.nilin.opex.common.translation

import co.nilin.opex.common.data.MessageTranslation
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

inline fun <reified T : Any?> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

@Component
//@ConditionalOnProperty(name = ["translation.config.enabled"], havingValue = "true")
class ConfigClient(@Qualifier("configWebClient") private val webClient: WebClient
) {


    suspend fun getMessagesUpdatedAfter(lastUpdate: Long?): List<MessageTranslation>? {
        return webClient.get().uri("/messages") {
            it.queryParam("last-update", lastUpdate)
            it.build()
        }.retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono(typeRef<List<MessageTranslation>>())
                .log()
                .awaitFirst()
    }
}
