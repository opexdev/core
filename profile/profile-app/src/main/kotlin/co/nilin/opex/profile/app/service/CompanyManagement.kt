package co.nilin.opex.profile.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.app.dto.CompanyAdminResponse
import co.nilin.opex.profile.app.dto.CompanyResponse
import co.nilin.opex.profile.app.dto.LegalAccountAvailabilityRequest
import co.nilin.opex.profile.app.dto.RegisterLegalAccountRequest
import co.nilin.opex.profile.core.data.event.KycLevelUpdatedEvent
import co.nilin.opex.profile.core.data.kyc.KycLevel
import co.nilin.opex.profile.core.data.profile.Company
import co.nilin.opex.profile.core.data.profile.LegalAccountAvailability
import co.nilin.opex.profile.core.data.profile.LegalAccountRegistration
import co.nilin.opex.profile.core.spi.CompanyPersister
import co.nilin.opex.profile.core.spi.KycLevelUpdatedPublisher
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CompanyManagement(
    private val companyPersister: CompanyPersister,
    private val kycLevelUpdatedPublisher: KycLevelUpdatedPublisher
) {

    suspend fun ensureAvailable(request: LegalAccountAvailabilityRequest) {
        val missing = buildList {
            if (request.nationalCode.isBlank()) add("nationalCode")
            if (request.email.isBlank()) add("email")
            if (request.mobile.isBlank()) add("mobile")
            if (request.companyRegistrationNumber.isBlank()) add("companyRegistrationNumber")
            if (request.companyNationalId.isBlank()) add("companyNationalId")
        }
        if (missing.isNotEmpty())
            throw OpexError.InvalidRequestBody.exception("Missing required fields: ${missing.joinToString()}")

        companyPersister.ensureAvailable(
            LegalAccountAvailability(
                nationalCode = request.nationalCode,
                email = request.email,
                mobile = request.mobile,
                companyRegistrationNumber = request.companyRegistrationNumber,
                companyNationalId = request.companyNationalId
            )
        )
    }

    suspend fun registerLegalAccount(request: RegisterLegalAccountRequest, creator: String): CompanyAdminResponse {
        val company = companyPersister.registerLegalAccount(
            LegalAccountRegistration(
                uuid = request.uuid,
                firstName = request.firstName,
                lastName = request.lastName,
                nationalCode = request.nationalCode,
                email = request.email,
                mobile = request.mobile,
                companyRegistrationNumber = request.companyRegistrationNumber,
                companyNationalId = request.companyNationalId,
                representativeNationalCardKey = request.representativeNationalCardKey,
                introductionLetterKey = request.introductionLetterKey,
                articlesOfAssociationKey = request.articlesOfAssociationKey,
                officialNewspaperKey = request.officialNewspaperKey,
                boardMemberDocumentKeys = request.boardMemberDocumentKeys
            ),
            creator
        )

        kycLevelUpdatedPublisher.publish(
            KycLevelUpdatedEvent(request.uuid, KycLevel.LEVEL_2, LocalDateTime.now())
        )

        return company.toAdminResponse()
    }

    suspend fun getCompany(uuid: String): CompanyResponse {
        return findCompany(uuid).toResponse()
    }

    suspend fun getCompanyForAdmin(uuid: String): CompanyAdminResponse {
        return findCompany(uuid).toAdminResponse()
    }

    suspend fun getCompanyByNationalId(nationalId: String): CompanyAdminResponse {
        return (companyPersister.findByCompanyNationalId(nationalId)
            ?: throw OpexError.NotFound.exception("Company not found")).toAdminResponse()
    }

    private suspend fun findCompany(uuid: String): Company {
        return companyPersister.findByRepresentativeUuid(uuid)
            ?: throw OpexError.NotFound.exception("Company not found")
    }

    private fun Company.toResponse() = CompanyResponse(id!!, registrationNumber, nationalId, createDate, updateDate)

    private fun Company.toAdminResponse() = CompanyAdminResponse(
        id!!,
        registrationNumber,
        nationalId,
        representativeNationalCardKey,
        introductionLetterKey,
        articlesOfAssociationKey,
        officialNewspaperKey,
        boardMemberDocumentKeys,
        createDate,
        updateDate
    )
}
