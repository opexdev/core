package co.nilin.opex.profile.core.spi

import co.nilin.opex.profile.core.data.inquiry.ComparativeResponse
import co.nilin.opex.profile.core.data.inquiry.IbanInfoResponse
import co.nilin.opex.profile.core.data.inquiry.ShahkarResponse
import co.nilin.opex.profile.core.data.inquiry.VerifyOwnershipResponse
import java.time.LocalDateTime

interface InquiryProxy {
    suspend fun getShahkarInquiryResult(identifier: String, mobile: String): ShahkarResponse

    suspend fun getComparativeInquiryResult(
        nationalCode: String,
        birthDate: Long,
        firstName: String,
        lastName: String
    ): ComparativeResponse

    suspend fun verifyCardOwnership(
        cardNumber: String,
        nationalCode: String,
        birthDate: LocalDateTime
    ): VerifyOwnershipResponse

    suspend fun verifyIbanOwnership(
        iban: String,
        nationalCode: String,
        birthDate: LocalDateTime
    ): VerifyOwnershipResponse

    suspend fun getIbanInfoByCardNumber(cardNumber: String): IbanInfoResponse
    suspend fun getIbanInfoByIban(iban: String): IbanInfoResponse


}