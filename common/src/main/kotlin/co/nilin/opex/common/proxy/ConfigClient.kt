package co.nilin.opex.common.proxy

import co.nilin.opex.common.config.CommonWebClient
import co.nilin.opex.common.data.WebConfig
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component

inline fun <reified T : Any?> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

@Component
class ConfigClient(
    @Qualifier("CommonWebClient") private val webClient: CommonWebClient
) {

    suspend fun getWebConfig(): WebConfig {
        return webClient.delegate.get().uri("http://config:8080/web/v1") {
            it.build()
        }.retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono(typeRef<WebConfig>())
            .log()
            .awaitFirst()
    }
}
