package co.nilin.opex.profile.core.spi

import co.nilin.opex.profile.core.data.profile.ComparativeResponse
import java.time.LocalDateTime

interface InquiryProxy {
    suspend fun getShahkarInquiryResult(identifier: String, mobile: String): Boolean

    suspend fun getComparativeInquiryResult(
        identifier: String,
        birthDate: Long,
        firstName: String,
        lastName: String
    ): ComparativeResponse
}