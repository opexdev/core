package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.ActiveUserStat
import co.nilin.opex.api.core.inout.DailyLoginStat
import co.nilin.opex.api.core.spi.DeviceManagementProxy
import co.nilin.opex.common.OpexError
import kotlinx.coroutines.reactive.awaitFirstOrElse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class DeviceManagementProxyImpl(
    @Qualifier("nlbWebClient") private val webClient: WebClient
) : DeviceManagementProxy {

    @Value("\${app.device-management.url}")
    private lateinit var baseUrl: String

    override suspend fun getDailyLogins(
        token: String,
        fromMillis: Long,
        toMillis: Long
    ): List<DailyLoginStat> {
        return webClient.get()
            .uri("$baseUrl/admin/stats/daily-logins?from=$fromMillis&to=$toMillis")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<List<DailyLoginStat>>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get daily login stats") }
    }

    override suspend fun getActiveUsers(
        token: String,
        fromMillis: Long,
        toMillis: Long
    ): ActiveUserStat {
        return webClient.get()
            .uri("$baseUrl/admin/stats/active-users?from=$fromMillis&to=$toMillis")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<ActiveUserStat>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get active user stats") }
    }
}
