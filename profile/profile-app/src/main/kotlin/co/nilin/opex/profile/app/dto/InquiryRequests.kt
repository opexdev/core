package co.nilin.opex.profile.app.dto

data class ShahkarInquiryRequest(
    val nationalCode: String,
    val mobile: String
)

data class IdentitySimilarityInquiryRequest(
    val nationalCode: String,
    val birthDate: Long,
    val firstName: String,
    val lastName: String
)

data class CardOwnershipInquiryRequest(
    val cardNumber: String,
    val nationalCode: String,
    val birthDate: Long
)

data class IbanOwnershipInquiryRequest(
    val iban: String,
    val nationalCode: String,
    val birthDate: Long
)

data class CardInfoInquiryRequest(
    val cardNumber: String
)

data class IbanInfoInquiryRequest(
    val iban: String
)
