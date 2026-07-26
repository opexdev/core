package co.nilin.opex.auth.proxy

import co.nilin.opex.auth.config.DeviceManagementConfig
import co.nilin.opex.auth.data.Sessions
import co.nilin.opex.auth.data.SessionRequest
import co.nilin.opex.common.OpexError
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class DeviceManagementProxy(
    @Qualifier("deviceManagementClient")
    private val client: WebClient,
    private val deviceManagementConfig: DeviceManagementConfig
) {
    suspend fun getLastSessions(
        sessionRequest: SessionRequest,
        ): List<Sessions> {
        val url = "${deviceManagementConfig.url}/devices"

        return client.post()
            .uri(url)
            .bodyValue(sessionRequest)
            .retrieve()
            .onStatus({ it.is4xxClientError }) {
                throw OpexError.BadRequest.exception()
            }
            .onStatus({ it.is5xxServerError }) {
                throw OpexError.InternalServerError.exception()
            }
            .awaitBody()
    }
}