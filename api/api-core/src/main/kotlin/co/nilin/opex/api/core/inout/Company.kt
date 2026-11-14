package co.nilin.opex.api.core.inout

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

data class LegalAccountAvailabilityRequest(
    val nationalCode: String,
    val email: String,
    val mobile: String,
    val companyRegistrationNumber: String,
    val companyNationalId: String
)

data class RegisterLegalAccountRequest(
    val companyUuid: UUID,
    val uuid: String,
    val firstName: String,
    val lastName: String,
    val nationalCode: String,
    val email: String,
    val mobile: String,
    val companyName: String,
    val companyRegistrationNumber: String,
    val companyNationalId: String,
    val representativeNationalCardKey: String? = null,
    val introductionLetterKey: String? = null,
    val articlesOfAssociationKey: String? = null,
    val officialNewspaperKey: String? = null,
    val boardMemberDocumentKeys: List<String> = emptyList()
)

data class LegalAccountRegistrationResponse(
    val uuid: String,
    val company: CompanyAdminResponse,
    val passwordEmailSent: Boolean
)

data class UpdateCompanyRequest(
    val name: String? = null,
    val registrationNumber: String? = null,
    val nationalId: String? = null,
    val representativeNationalCardKey: String? = null,
    val introductionLetterKey: String? = null,
    val articlesOfAssociationKey: String? = null,
    val officialNewspaperKey: String? = null,
    val boardMemberDocumentKeys: List<String>? = null
)

