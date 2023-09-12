package co.nilin.opex.admin.app.proxy

import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class BlockchainGatewayProxy(
    @Value("\${app.scheduler.url}")
    private val schedulerUrl: String,
    private val webClient: WebClient,
) {

    suspend fun manualSync(network: String, block: Long) {
        webClient.get()
            .uri("$schedulerUrl/$network/$block")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<Void>()
            .awaitFirstOrNull()
    }

}