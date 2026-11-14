package co.nilin.opex.profile.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.app.dto.CompanyAdminResponse
import co.nilin.opex.profile.app.dto.CompanyResponse
import co.nilin.opex.profile.app.dto.LegalAccountAvailabilityRequest
import co.nilin.opex.profile.app.dto.RegisterLegalAccountRequest
import co.nilin.opex.profile.app.dto.UpdateCompanyRequest
import co.nilin.opex.profile.core.data.event.KycLevelUpdatedEvent
import co.nilin.opex.profile.core.data.kyc.KycLevel
import co.nilin.opex.profile.core.data.profile.Company
import co.nilin.opex.profile.core.data.profile.CompanyUpdate
import co.nilin.opex.profile.core.data.profile.LegalAccountAvailability
import co.nilin.opex.profile.core.data.profile.LegalAccountRegistration
import co.nilin.opex.profile.core.spi.CompanyPersister
import co.nilin.opex.profile.core.spi.KycLevelUpdatedPublisher
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

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
                companyUuid = request.companyUuid,
                uuid = request.uuid,
                firstName = request.firstName,
                lastName = request.lastName,
                nationalCode = request.nationalCode,
                email = request.email,
                mobile = request.mobile,
                companyName = request.companyName,
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

    suspend fun getCompanyByUuid(companyUuid: UUID): CompanyAdminResponse {
        return (companyPersister.findByUuid(companyUuid)
            ?: throw OpexError.NotFound.exception("Company not found" )).toAdminResponse()
    }

    suspend fun updateCompany(companyUuid: UUID, request: UpdateCompanyRequest): CompanyAdminResponse {
        val update = CompanyUpdate(
            name = request.name?.trim(),
            registrationNumber = request.registrationNumber?.trim(),
            nationalId = request.nationalId?.trim(),
            representativeNationalCardKey = request.representativeNationalCardKey?.trim(),
            introductionLetterKey = request.introductionLetterKey?.trim(),
            articlesOfAssociationKey = request.articlesOfAssociationKey?.trim(),
            officialNewspaperKey = request.officialNewspaperKey?.trim(),
            boardMemberDocumentKeys = request.boardMemberDocumentKeys?.map { it.trim() }
        )

        val fields = mapOf(
            "name" to update.name,
            "registrationNumber" to update.registrationNumber,
            "nationalId" to update.nationalId,
            "representativeNationalCardKey" to update.representativeNationalCardKey,
            "introductionLetterKey" to update.introductionLetterKey,
            "articlesOfAssociationKey" to update.articlesOfAssociationKey,
            "officialNewspaperKey" to update.officialNewspaperKey
        )

        if (fields.values.all { it == null } && update.boardMemberDocumentKeys.isNullOrEmpty())
            throw OpexError.InvalidRequestBody.exception("At least one field must be provided")

        val blank = buildList {
            fields.forEach { (field, value) -> if (value?.isEmpty() == true) add(field) }
            if (update.boardMemberDocumentKeys?.any { it.isEmpty() } == true) add("boardMemberDocumentKeys")
        }
        if (blank.isNotEmpty())
            throw OpexError.InvalidRequestBody.exception("Fields must not be blank: ${blank.joinToString()}")

        return companyPersister.updateByUuid(companyUuid, update).toAdminResponse()
    }

    private suspend fun findCompany(uuid: String): Company {
        return companyPersister.findByRepresentativeUuid(uuid)
            ?: throw OpexError.NotFound.exception("Company not found")
    }

    private fun Company.toResponse() =
        CompanyResponse(id!!, name, registrationNumber, nationalId, createDate, updateDate, companyUuid)

    private fun Company.toAdminResponse() = CompanyAdminResponse(
        id!!,
        name,
        registrationNumber,
        nationalId,
        representativeNationalCardKey,
        introductionLetterKey,
        articlesOfAssociationKey,
        officialNewspaperKey,
        boardMemberDocumentKeys,
        createDate,
        updateDate,
        companyUuid
    )
}
