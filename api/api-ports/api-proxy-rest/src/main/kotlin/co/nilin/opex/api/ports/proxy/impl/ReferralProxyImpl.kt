package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.CheckoutStatus
import co.nilin.opex.api.core.inout.CreateReferralCodeRequest
import co.nilin.opex.api.core.inout.ReferralCodeDto
import co.nilin.opex.api.core.inout.RewardClaimCheckoutErrorDto
import co.nilin.opex.api.core.inout.RewardClaimDto
import co.nilin.opex.api.core.inout.UpdateRewardClaimStatusRequest
import co.nilin.opex.api.core.spi.ReferralProxy
import co.nilin.opex.common.OpexError
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
class ReferralProxyImpl(@Qualifier("generalWebClient") private val webClient: WebClient) : ReferralProxy {

    @Value("\${app.referral.url}")
    private lateinit var baseUrl: String

    override suspend fun getCommissionShareSteps(): List<Int> {
        return webClient.get()
            .uri("$baseUrl/codes/commission-share-steps")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<List<Int>>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get commission share steps") }
    }

    override suspend fun generateReferralCode(token: String, commissionShare: Int): String {
        return webClient.post()
            .uri("$baseUrl/codes")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(CreateReferralCodeRequest(commissionShare)))
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<String>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to generate referral code") }
    }

    override suspend fun getReferralCode(code: String): ReferralCodeDto {
        return webClient.get()
            .uri("$baseUrl/codes/$code")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<ReferralCodeDto>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get referral code $code") }
    }

    override suspend fun getReferralCodes(token: String): List<ReferralCodeDto> {
        return webClient.get()
            .uri("$baseUrl/codes")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<List<ReferralCodeDto>>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get referral codes") }
    }

    override suspend fun getRewardClaims(
        token: String,
        status: CheckoutStatus?,
        uuid: String?,
        offset: Int,
        limit: Int
    ): List<RewardClaimDto> {
        return webClient.get()
            .uri("$baseUrl/admin/reward-claims") {
                if (status != null) it.queryParam("status", status)
                if (uuid != null) it.queryParam("uuid", uuid)
                it.queryParam("offset", offset)
                it.queryParam("limit", limit)
                it.build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<List<RewardClaimDto>>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get reward claims") }
    }

    override suspend fun getRewardClaimErrors(token: String, id: Long): List<RewardClaimCheckoutErrorDto> {
        return webClient.get()
            .uri("$baseUrl/admin/reward-claims/$id/errors")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<List<RewardClaimCheckoutErrorDto>>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get errors of reward claim $id") }
    }

    override suspend fun retryRewardClaim(token: String, id: Long): RewardClaimDto {
        return webClient.post()
            .uri("$baseUrl/admin/reward-claims/$id/retry")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<RewardClaimDto>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to retry reward claim $id") }
    }

    override suspend fun updateRewardClaimStatus(token: String, id: Long, status: CheckoutStatus): RewardClaimDto {
        return webClient.put()
            .uri("$baseUrl/admin/reward-claims/$id/status")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(UpdateRewardClaimStatusRequest(status)))
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<RewardClaimDto>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to update status of reward claim $id") }
    }
}
