package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.UpdateUserConfigRequest
import co.nilin.opex.api.core.inout.UpdateWebConfigRequest
import co.nilin.opex.api.core.inout.UserLevelConfig
import co.nilin.opex.api.core.inout.UserWebConfig
import co.nilin.opex.api.core.spi.ConfigProxy
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.data.UserLanguage
import co.nilin.opex.common.data.WebConfig
import co.nilin.opex.common.utils.LanguageUtils.getUserLanguage
import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.*
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
        return webClient.post()
            .uri("$baseUrl/web/v1")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<WebConfig>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun getUserLevelConfig(): List<UserLevelConfig> {
        val lang = UserLanguage.safeValueOf(getUserLanguage().awaitSingleOrNull()).toString()
        return webClient.get()
            .uri("$baseUrl/user-level/v1") {
                it.queryParam("language", lang)
                it.build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToFlux<UserLevelConfig>()
            .collectList()
            .awaitSingle()
    }

    override suspend fun updateUserLevelConfig(
        token: String,
        userLevelConfig: UserLevelConfig
    ): UserLevelConfig {
        return webClient.post()
            .uri("$baseUrl/user-level/v1")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(userLevelConfig))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<UserLevelConfig>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun deleteUserLevelConfig(
        token: String,
        userLevel: String,
        language: String
    ) {
        webClient.delete()
            .uri("$baseUrl/user-level/v1/${userLevel}/${language}")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }) { response ->
                response.createException()
            }
            .awaitBodilessEntity()
    }

    override suspend fun getUserConfig(token: String): UserWebConfig {
        return webClient.get()
            .uri("$baseUrl/user/v1")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<UserWebConfig>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get user config") }
    }

    override suspend fun updateUserConfig(
        token: String,
        request: UpdateUserConfigRequest
    ): UserWebConfig {
        return webClient.post()
            .uri("$baseUrl/user/v1")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<UserWebConfig>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun addUserFavoritePair(
        token: String,
        pairs: Set<String>
    ): UserWebConfig {
        return webClient.post()
            .uri("$baseUrl/user/v1/pair")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(pairs))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<UserWebConfig>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun removeUserFavoritePair(
        token: String,
        pairs: Set<String>
    ): UserWebConfig {
        return webClient.method(HttpMethod.DELETE)
            .uri("$baseUrl/user/v1/pair")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .bodyValue(pairs)
            .retrieve()
            .onStatus({ it.isError }) { it.createException() }
            .bodyToMono<UserWebConfig>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }
}

