package co.nilin.opex.profile.app.dto

import java.time.LocalDateTime
import java.util.UUID

data class CompanyResponse(
    val id: Long,
    val name: String?,
    val registrationNumber: String,
    val nationalId: String,
    val createDate: LocalDateTime?,
    val updateDate: LocalDateTime?,
    val companyUuid: UUID
)

// Document keys are admin-only and must never reach the representative
data class CompanyAdminResponse(
    val id: Long,
    val name: String?,
    val registrationNumber: String,
    val nationalId: String,
    val representativeNationalCardKey: String?,
    val introductionLetterKey: String?,
    val articlesOfAssociationKey: String?,
    val officialNewspaperKey: String?,
    val boardMemberDocumentKeys: List<String>,
    val createDate: LocalDateTime?,
    val updateDate: LocalDateTime?,
    val companyUuid: UUID
)
