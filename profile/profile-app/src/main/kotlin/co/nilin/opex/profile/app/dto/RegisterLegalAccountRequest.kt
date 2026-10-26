package co.nilin.opex.profile.app.dto

data class RegisterLegalAccountRequest(
    val uuid: String,
    val firstName: String,
    val lastName: String,
    val nationalCode: String,
    val email: String,
    val mobile: String,
    val companyRegistrationNumber: String,
    val companyNationalId: String,
    val representativeNationalCardKey: String? = null,
    val introductionLetterKey: String? = null,
    val articlesOfAssociationKey: String? = null,
    val officialNewspaperKey: String? = null,
    val boardMemberDocumentKeys: List<String> = emptyList()
)
