package co.nilin.opex.common.proxy

import co.nilin.opex.common.config.CommonWebClient
import co.nilin.opex.common.data.WebConfig
import co.nilin.opex.common.utils.typeRef
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component


@Component
@ConditionalOnProperty(name = ["app.custom-user-language.enabled"], havingValue = "true", matchIfMissing = false)
class CustomUserLanguageClient(
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
