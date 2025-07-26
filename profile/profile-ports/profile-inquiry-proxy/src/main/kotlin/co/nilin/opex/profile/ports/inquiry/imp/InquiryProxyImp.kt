package co.nilin.opex.profile.ports.inquiry.imp

import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.core.spi.InquiryProxy
import co.nilin.opex.profile.ports.inquiry.data.ComparativeResponse
import co.nilin.opex.profile.ports.inquiry.data.ShahkarResponse
import co.nilin.opex.profile.ports.inquiry.utils.TokenProvider
import co.nilin.opex.profile.ports.inquiry.utils.toPersianDateFormatted
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.time.LocalDateTime

@Component
class InquiryProxyImp(
    @Qualifier("plainWebClient") private val webClient: WebClient,
    private val tokenProvider: TokenProvider
) : InquiryProxy {

    @Value("\${inquiry.url}")
    private lateinit var baseUrl: String

    override suspend fun getShahkarInquiryResult(identifier: String, mobile: String): Boolean {
        return webClient.get()
            .uri("$baseUrl/v1/services/matching") {
                it.queryParam("nationalCode", identifier)
                it.queryParam("mobileNumber", mobile)
                it.build()
            }
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${tokenProvider.getToken()}")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus({ t -> t.isError }, { throw OpexError.ShahkarInquiryUnavailable.exception() })
            .awaitBody<ShahkarResponse>()
            .matched
    }

    override suspend fun getComparativeInquiryResult(
        identifier: String,
        birthDate: LocalDateTime,
        firstName: String,
        lastName: String
    ): Boolean {
        val birthDateFormatted = birthDate.toPersianDateFormatted()

        val response = webClient.get()
            .uri("$baseUrl/v1/services/identity/similarity") {
                it.queryParam("nationalCode", identifier)
                it.queryParam("birthDate", birthDateFormatted)
                it.queryParam("firstName", firstName)
                it.queryParam("lastName", lastName)
                it.build()
            }
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${tokenProvider.getToken()}")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus({ t -> t.isError }, { throw OpexError.ComparativeInquiryUnavailable.exception() })
            .awaitBody<ComparativeResponse>()

        return (response.firstNameSimilarityPercentage >= 95 && response.lastNameSimilarityPercentage >= 95)
    }
}