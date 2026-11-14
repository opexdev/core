package co.nilin.opex.profile.core.data.profile

data class CompanyUpdate(
    val name: String? = null,
    val registrationNumber: String? = null,
    val nationalId: String? = null,
    val representativeNationalCardKey: String? = null,
    val introductionLetterKey: String? = null,
    val articlesOfAssociationKey: String? = null,
    val officialNewspaperKey: String? = null,
    val boardMemberDocumentKeys: List<String>? = null
)
