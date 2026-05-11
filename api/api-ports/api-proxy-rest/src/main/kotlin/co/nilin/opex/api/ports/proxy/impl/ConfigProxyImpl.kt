package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.UpdateWebConfigRequest
import co.nilin.opex.api.core.spi.ConfigProxy
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.data.WebConfig
import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.reactive.awaitFirstOrElse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.body
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Component
class ConfigProxyImpl(@Qualifier("generalWebClient") private val webClient: WebClient) : ConfigProxy {

    private val logger by LoggerDelegate()

    @Value("\${app.config.url}")
    private lateinit var baseUrl: String

    override suspend fun getWebConfig(): WebConfig {
        return webClient.get()
            .uri("$baseUrl/web/v1")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<WebConfig>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get web config") }
    }

    override suspend fun updateWebConfig(
        token: String,
        request: UpdateWebConfigRequest
    ): WebConfig {
        return webClient.put()
            .uri("$baseUrl/web/v1")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<WebConfig>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }
}
