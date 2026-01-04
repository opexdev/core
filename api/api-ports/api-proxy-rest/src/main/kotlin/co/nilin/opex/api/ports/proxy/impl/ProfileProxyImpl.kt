package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.CompleteProfileRequest
import co.nilin.opex.api.core.ContactUpdateConfirmRequest
import co.nilin.opex.api.core.ContactUpdateRequest
import co.nilin.opex.api.core.ProfileApprovalUserResponse
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
import org.springframework.web.reactive.function.client.awaitBodilessEntity
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

    override suspend fun getProfileAdmin(token: String, uuid: String): Profile {
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

    override suspend fun addAddressBook(
        token: String,
        request: AddAddressBookItemRequest
    ): AddressBookResponse {
        return webClient.post()
            .uri("$baseUrl/address-book")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<AddressBookResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to add address book") }
    }

    override suspend fun getAllAddressBooks(token: String): List<AddressBookResponse> {
        return webClient.get()
            .uri("$baseUrl/address-book`")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<List<AddressBookResponse>>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get address books") }
    }

    override suspend fun deleteAddressBook(
        token: String,
        id: Long
    ) {
        webClient.delete()
            .uri("$baseUrl/address-book/$id")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }) { response ->
                response.createException()
            }
            .awaitBodilessEntity()
    }

    override suspend fun updateAddressBook(
        token: String,
        id: Long,
        request: AddAddressBookItemRequest
    ): AddressBookResponse {
        return webClient.put()
            .uri("$baseUrl/address-book/$id")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<AddressBookResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to update address book") }
    }

    override suspend fun addBankAccount(
        token: String,
        request: AddBankAccountRequest
    ): BankAccountResponse {
        return webClient.post()
            .uri("$baseUrl/bank-account")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<BankAccountResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to add bank account") }
    }

    override suspend fun getBankAccounts(token: String): List<BankAccountResponse> {
        return webClient.get()
            .uri("$baseUrl/bank-account`")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<List<BankAccountResponse>>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get bank accounts") }
    }

    override suspend fun deleteBankAccount(token: String, id: Long) {
        webClient.delete()
            .uri("$baseUrl/bank-account/$id")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }) { response ->
                response.createException()
            }
            .awaitBodilessEntity()
    }

    override suspend fun getProfile(token: String): Profile {
        return webClient.get()
            .uri("$baseUrl/personal-data")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<Profile>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get profile") }
    }

    override suspend fun completeProfile(
        token: String,
        request: CompleteProfileRequest
    ): Profile? {
        return webClient.put()
            .uri("$baseUrl/completion")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<Profile>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to complete profile") }
    }

    override suspend fun requestContactUpdate(
        token: String,
        request: ContactUpdateRequest
    ): TempOtpResponse {
        return webClient.post()
            .uri("$baseUrl/contact/update/otp-request")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<TempOtpResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to request contact update") }
    }

    override suspend fun confirmContactUpdate(
        token: String,
        request: ContactUpdateConfirmRequest
    ) {
        webClient.patch()
            .uri("$baseUrl/contact/update/otp-verification")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .bodyValue(request)
            .retrieve()
            .onStatus({ it.isError }) { response ->
                response.createException()
            }
            .awaitBodilessEntity()
    }

    override suspend fun getUserProfileApprovalRequest(token: String): ProfileApprovalUserResponse {
        return webClient.get()
            .uri("$baseUrl/approval-request")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<ProfileApprovalUserResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get profile approval request") }
    }
}

