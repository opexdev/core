package co.nilin.opex.api.core.inout

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

data class ShahkarInquiryResponse(
    val matched: Boolean? = null,
    val code: String? = null,
    val message: String? = null
)

data class IdentitySimilarityInquiryResponse(
    val firstNameSimilarityPercentage: Int? = null,
    val lastNameSimilarityPercentage: Int? = null,
    val code: String? = null,
    val message: String? = null
)

data class VerifyOwnershipInquiryResponse(
    val matched: Boolean? = null,
    val code: String? = null,
    val message: String? = null
)

data class IbanInfoInquiryResponse(
    val ibanInfo: IbanInfoDetails? = null,
    val code: String? = null,
    val message: String? = null
)

data class IbanInfoDetails(
    val bank: String? = null,
    val depositNumber: String? = null,
    val iban: String? = null
)
