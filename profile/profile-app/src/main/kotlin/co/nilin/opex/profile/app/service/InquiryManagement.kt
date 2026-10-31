package co.nilin.opex.profile.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.app.dto.CardInfoInquiryRequest
import co.nilin.opex.profile.app.dto.CardOwnershipInquiryRequest
import co.nilin.opex.profile.app.dto.IbanInfoInquiryRequest
import co.nilin.opex.profile.app.dto.IbanOwnershipInquiryRequest
import co.nilin.opex.profile.app.dto.IdentitySimilarityInquiryRequest
import co.nilin.opex.profile.app.dto.ShahkarInquiryRequest
import co.nilin.opex.profile.core.data.inquiry.ComparativeResponse
import co.nilin.opex.profile.core.data.inquiry.IbanInfoResponse
import co.nilin.opex.profile.core.data.inquiry.ShahkarResponse
import co.nilin.opex.profile.core.data.inquiry.VerifyOwnershipResponse
import co.nilin.opex.profile.core.spi.InquiryProxy
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class InquiryManagement(
    private val inquiryProxy: InquiryProxy
) {

    private val logger = LoggerFactory.getLogger(InquiryManagement::class.java)

    suspend fun shahkar(request: ShahkarInquiryRequest, adminId: String): ShahkarResponse {
        validate("nationalCode" to request.nationalCode, "mobile" to request.mobile)
        logger.info("Admin $adminId is running a shahkar inquiry")
        return inquiryProxy.getShahkarInquiryResult(request.nationalCode, request.mobile)
    }

    suspend fun identitySimilarity(
        request: IdentitySimilarityInquiryRequest,
        adminId: String
    ): ComparativeResponse {
        validate(
            "nationalCode" to request.nationalCode,
            "firstName" to request.firstName,
            "lastName" to request.lastName
        )
        logger.info("Admin $adminId is running an identity similarity inquiry")
        return inquiryProxy.getComparativeInquiryResult(
            request.nationalCode,
            request.birthDate,
            request.firstName,
            request.lastName
        )
    }

    suspend fun cardOwnership(request: CardOwnershipInquiryRequest, adminId: String): VerifyOwnershipResponse {
        validate("cardNumber" to request.cardNumber, "nationalCode" to request.nationalCode)
        logger.info("Admin $adminId is running a card ownership inquiry")
        return inquiryProxy.verifyCardOwnership(
            request.cardNumber,
            request.nationalCode,
            request.birthDate.toLocalDateTime()
        )
    }

    suspend fun ibanOwnership(request: IbanOwnershipInquiryRequest, adminId: String): VerifyOwnershipResponse {
        validate("iban" to request.iban, "nationalCode" to request.nationalCode)
        logger.info("Admin $adminId is running an iban ownership inquiry")
        return inquiryProxy.verifyIbanOwnership(
            request.iban,
            request.nationalCode,
            request.birthDate.toLocalDateTime()
        )
    }

    suspend fun cardInfo(request: CardInfoInquiryRequest, adminId: String): IbanInfoResponse {
        validate("cardNumber" to request.cardNumber)
        logger.info("Admin $adminId is running a card to iban inquiry")
        return inquiryProxy.getIbanInfoByCardNumber(request.cardNumber)
    }

    suspend fun ibanInfo(request: IbanInfoInquiryRequest, adminId: String): IbanInfoResponse {
        validate("iban" to request.iban)
        logger.info("Admin $adminId is running an iban info inquiry")
        return inquiryProxy.getIbanInfoByIban(request.iban)
    }

    private fun validate(vararg fields: Pair<String, String>) {
        fields.firstOrNull { it.second.isBlank() }?.let {
            throw OpexError.InvalidRequestBody.exception("${it.first} is required")
        }
    }

    private fun Long.toLocalDateTime(): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
}
