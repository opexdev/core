package co.nilin.opex.profile.core.data.profile

import java.time.LocalDateTime

data class Company(
    var id: Long? = null,
    var registrationNumber: String,
    var nationalId: String,
    var representativeNationalCardKey: String? = null,
    var introductionLetterKey: String? = null,
    var articlesOfAssociationKey: String? = null,
    var officialNewspaperKey: String? = null,
    var boardMemberDocumentKeys: List<String> = emptyList(),
    var createDate: LocalDateTime? = null,
    var updateDate: LocalDateTime? = null,
    var creator: String? = null
)

data class CompanyRepresentative(
    var id: Long? = null,
    var companyId: Long,
    var uuid: String,
    var createDate: LocalDateTime? = null,
    var creator: String? = null
)
