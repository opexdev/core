package co.nilin.opex.profile.core.spi

import java.time.LocalDateTime

interface InquiryProxy {
    suspend fun getShahkarInquiryResult(identifier: String, mobile: String): Boolean

    suspend fun getComparativeInquiryResult(
        identifier: String,
        birthDate: LocalDateTime,
        firstName: String,
        lastName: String
    ): Boolean
}