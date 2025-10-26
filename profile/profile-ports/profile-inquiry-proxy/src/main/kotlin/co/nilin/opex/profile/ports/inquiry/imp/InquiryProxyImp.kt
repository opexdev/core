package co.nilin.opex.profile.ports.inquiry.imp

import co.nilin.opex.profile.core.data.inquiry.ComparativeResponse
import co.nilin.opex.profile.core.data.inquiry.IbanInfoResponse
import co.nilin.opex.profile.core.data.inquiry.ShahkarResponse
import co.nilin.opex.profile.core.data.inquiry.VerifyOwnershipResponse
import co.nilin.opex.profile.core.spi.InquiryProxy
import co.nilin.opex.profile.ports.inquiry.utils.TokenProvider
import co.nilin.opex.profile.ports.inquiry.utils.toPersianDateFormatted
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDateTime

@Component
class InquiryProxyImp(
    @Qualifier("plainWebClient") private val webClient: WebClient, private val tokenProvider: TokenProvider
) : InquiryProxy {

    @Value("\${app.inquiry.url}")
    private lateinit var baseUrl: String

    override suspend fun getShahkarInquiryResult(identifier: String, mobile: String): ShahkarResponse {
        return webClient.get().uri("$baseUrl/v1/services/matching") {
            it.queryParam("nationalCode", identifier)
            it.queryParam("mobileNumber", mobile)
            it.build()
        }.header(HttpHeaders.AUTHORIZATION, "Bearer ${tokenProvider.getToken()}").accept(MediaType.APPLICATION_JSON)
            .exchangeToMono { clientResponse ->
                clientResponse.bodyToMono(ShahkarResponse::class.java)
            }.awaitSingle()
    }

    override suspend fun getComparativeInquiryResult(
        nationalCode: String, birthDate: Long, firstName: String, lastName: String
    ): ComparativeResponse {
        val birthDateFormatted = birthDate.toPersianDateFormatted()

        return webClient.get().uri("$baseUrl/v1/services/identity/similarity") {
            it.queryParam("nationalCode", nationalCode)
            it.queryParam("birthDate", birthDateFormatted)
            it.queryParam("firstName", firstName)
            it.queryParam("lastName", lastName)
            it.build()
        }.header(HttpHeaders.AUTHORIZATION, "Bearer ${tokenProvider.getToken()}").accept(MediaType.APPLICATION_JSON)
            .exchangeToMono { clientResponse ->
                clientResponse.bodyToMono(ComparativeResponse::class.java)
            }.awaitSingle()
    }

    override suspend fun verifyCardOwnership(
        cardNumber: String, nationalCode: String, birthDate: LocalDateTime
    ): VerifyOwnershipResponse {
        val birthDateFormatted = birthDate.toPersianDateFormatted()

        return webClient.get().uri("$baseUrl/v1/services/matching") {
            it.queryParam("cardNumber", cardNumber)
            it.queryParam("nationalCode", nationalCode)
            it.queryParam("birthDate", birthDateFormatted)
            it.build()
        }.header(HttpHeaders.AUTHORIZATION, "Bearer ${tokenProvider.getToken()}").accept(MediaType.APPLICATION_JSON)
            .exchangeToMono { clientResponse ->
                clientResponse.bodyToMono(VerifyOwnershipResponse::class.java)
            }.awaitSingle()
    }


    override suspend fun verifyIbanOwnership(
        iban: String, nationalCode: String, birthDate: LocalDateTime
    ): VerifyOwnershipResponse {
        val birthDateFormatted = birthDate.toPersianDateFormatted()

        return webClient.get().uri("$baseUrl/v1/services/matching") {
            it.queryParam("iban", iban)
            it.queryParam("nationalCode", nationalCode)
            it.queryParam("birthDate", birthDateFormatted)
            it.build()
        }.header(HttpHeaders.AUTHORIZATION, "Bearer ${tokenProvider.getToken()}").accept(MediaType.APPLICATION_JSON)
            .exchangeToMono { clientResponse ->
                clientResponse.bodyToMono(VerifyOwnershipResponse::class.java)
            }.awaitSingle()
    }

    override suspend fun getIbanInfoByCardNumber(cardNumber: String): IbanInfoResponse {
        return webClient.get().uri("$baseUrl/v1/cards") {
            it.queryParam("number", cardNumber)
            it.queryParam("iban", true)
            it.build()
        }.header(HttpHeaders.AUTHORIZATION, "Bearer ${tokenProvider.getToken()}").accept(MediaType.APPLICATION_JSON)
            .exchangeToMono { clientResponse ->
                clientResponse.bodyToMono(IbanInfoResponse::class.java)
            }.awaitSingle()
    }

    override suspend fun getIbanInfoByIban(iban: String): IbanInfoResponse {
        return webClient.get().uri("$baseUrl/v1/ibans") {
            it.queryParam("value", iban)
            it.build()
        }.header(HttpHeaders.AUTHORIZATION, "Bearer ${tokenProvider.getToken()}").accept(MediaType.APPLICATION_JSON)
            .exchangeToMono { clientResponse ->
                clientResponse.bodyToMono(IbanInfoResponse::class.java)
            }.awaitSingle()
    }
}
