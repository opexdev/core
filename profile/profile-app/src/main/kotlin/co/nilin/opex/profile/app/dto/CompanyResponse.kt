package co.nilin.opex.profile.app.dto

import java.time.LocalDateTime

data class CompanyResponse(
    val id: Long,
    val registrationNumber: String,
    val nationalId: String,
    val createDate: LocalDateTime?,
    val updateDate: LocalDateTime?
)

// Document keys are admin-only and must never reach the representative
data class CompanyAdminResponse(
    val id: Long,
    val registrationNumber: String,
    val nationalId: String,
    val representativeNationalCardKey: String?,
    val introductionLetterKey: String?,
    val articlesOfAssociationKey: String?,
    val officialNewspaperKey: String?,
    val boardMemberDocumentKeys: List<String>,
    val createDate: LocalDateTime?,
    val updateDate: LocalDateTime?
)
