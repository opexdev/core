package co.nilin.opex.profile.core.spi

interface ShahkarInquiry {
    suspend fun getInquiryResult(identifier: String, mobile: String): Boolean
}