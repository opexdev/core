package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.ProfileProxy
import co.nilin.opex.common.OpexError
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
class ProfileProxyImpl(@Qualifier("generalWebClient") private val webClient: WebClient) : ProfileProxy {

    private val logger by LoggerDelegate()

    @Value("\${app.profile.url}")
    private lateinit var baseUrl: String

    override suspend fun getProfiles(
        token: String,
        profileRequest: ProfileRequest,
    ): List<Profile> {
        return webClient.post()
            .uri("$baseUrl/admin/profile")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(profileRequest))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<List<Profile>>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get profiles") }
    }

    override suspend fun getProfile(token: String, uuid: String): Profile {
        return webClient.get()
            .uri("$baseUrl/admin/profile/$uuid")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<Profile>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get profile of $uuid") }
    }

    override suspend fun getProfileHistory(
        token: String,
        uuid: String,
        limit: Int,
        offset: Int
    ): List<ProfileHistory> {
        return webClient.get()
            .uri("$baseUrl/admin/profile/history/$uuid?limit=$limit&offset=$offset")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<List<ProfileHistory>>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get profile history of $uuid") }
    }

    override suspend fun getProfileApprovalRequests(
        token: String,
        request: ProfileApprovalRequestFilter
    ): List<ProfileApprovalAdminResponse> {
        return webClient.post()
            .uri("$baseUrl/admin/profile/approval-requests")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<List<ProfileApprovalAdminResponse>>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get profile approval requests") }
    }

    override suspend fun getProfileApprovalRequest(
        token: String,
        requestId: Long
    ): ProfileApprovalAdminResponse {
        return webClient.get()
            .uri("$baseUrl/admin/profile/approval-request/$requestId")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<ProfileApprovalAdminResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get profile approval request $requestId") }
    }

    override suspend fun updateProfileApprovalRequest(
        token: String,
        request: UpdateApprovalRequestBody
    ): ProfileApprovalAdminResponse {
        return webClient.put()
            .uri("$baseUrl/admin/profile/approval-request")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<ProfileApprovalAdminResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to update profile approval request ${request.id}") }
    }
}

