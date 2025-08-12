package co.nilin.opex.profile.core.spi

import co.nilin.opex.profile.core.data.profile.ComparativeResponse
import co.nilin.opex.profile.core.data.profile.ShahkarResponse
import java.time.LocalDateTime

interface InquiryProxy {
    suspend fun getShahkarInquiryResult(identifier: String, mobile: String): ShahkarResponse

    suspend fun getComparativeInquiryResult(
        identifier: String,
        birthDate: Long,
        firstName: String,
        lastName: String
    ): ComparativeResponse
}