package co.nilin.opex.profile.ports.postgres.imp

import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.core.data.kyc.KycLevel
import co.nilin.opex.profile.core.data.profile.Company
import co.nilin.opex.profile.core.data.profile.CompanyUpdate
import co.nilin.opex.profile.core.data.profile.LegalAccountAvailability
import co.nilin.opex.profile.core.data.profile.LegalAccountRegistration
import co.nilin.opex.profile.core.data.profile.PersonType
import co.nilin.opex.profile.core.data.profile.ProfileStatus
import co.nilin.opex.profile.core.spi.CompanyPersister
import co.nilin.opex.profile.ports.postgres.dao.CompanyBoardMemberDocumentRepository
import co.nilin.opex.profile.ports.postgres.dao.CompanyRepository
import co.nilin.opex.profile.ports.postgres.dao.CompanyRepresentativeRepository
import co.nilin.opex.profile.ports.postgres.dao.ProfileRepository
import co.nilin.opex.profile.ports.postgres.model.entity.CompanyBoardMemberDocumentModel
import co.nilin.opex.profile.ports.postgres.model.entity.CompanyModel
import co.nilin.opex.profile.ports.postgres.model.entity.CompanyRepresentativeModel
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class CompanyManagementImp(
    private val companyRepository: CompanyRepository,
    private val companyRepresentativeRepository: CompanyRepresentativeRepository,
    private val boardMemberDocumentRepository: CompanyBoardMemberDocumentRepository,
    private val profileRepository: ProfileRepository
) : CompanyPersister {

    override suspend fun ensureAvailable(availability: LegalAccountAvailability) {
        profileRepository.findLatestByIdentifier(availability.nationalCode).awaitFirstOrNull()?.let {
            throw OpexError.NationalCodeAlreadyExists.exception()
        }
        profileRepository.findByEmail(availability.email)?.awaitFirstOrNull()?.let {
            throw OpexError.EmailAlreadyExists.exception()
        }
        profileRepository.findByMobile(availability.mobile)?.awaitFirstOrNull()?.let {
            throw OpexError.MobileAlreadyExists.exception()
        }

        companyRepository
            .findByRegistrationNumberOrNationalId(
                availability.companyRegistrationNumber,
                availability.companyNationalId
            )
            .awaitFirstOrNull()
            ?.let { found ->
                if (found.registrationNumber != availability.companyRegistrationNumber ||
                    found.nationalId != availability.companyNationalId
                ) throw OpexError.CompanyIdentifiersMismatch.exception()
                throw OpexError.CompanyAlreadyRegistered.exception()
            }
    }

    @Transactional
    override suspend fun registerLegalAccount(registration: LegalAccountRegistration, creator: String): Company {
        upsertRepresentativeProfile(registration, creator)

        val saved = saveCompanyAndLink(
            Company(
                companyUuid = registration.companyUuid,
                name = registration.companyName,
                registrationNumber = registration.companyRegistrationNumber,
                nationalId = registration.companyNationalId,
                representativeNationalCardKey = registration.representativeNationalCardKey,
                introductionLetterKey = registration.introductionLetterKey,
                articlesOfAssociationKey = registration.articlesOfAssociationKey,
                officialNewspaperKey = registration.officialNewspaperKey,
                creator = creator
            ),
            registration.uuid
        )

        registration.boardMemberDocumentKeys.forEach { key ->
            boardMemberDocumentRepository.save(
                CompanyBoardMemberDocumentModel().apply {
                    this.companyId = saved.id!!
                    s3Key = key
                    createDate = LocalDateTime.now()
                }
            ).awaitFirstOrNull() ?: throw OpexError.BadRequest.exception("Failed to save board member document")
        }

        return saved.toCompany(registration.boardMemberDocumentKeys)
    }

    override suspend fun findByRepresentativeUuid(uuid: String): Company? {
        return companyRepository.findByRepresentativeUuid(uuid).awaitFirstOrNull()?.withBoardMembers()
    }

    override suspend fun findByCompanyNationalId(nationalId: String): Company? {
        return companyRepository.findByNationalId(nationalId).awaitFirstOrNull()?.withBoardMembers()
    }

    override suspend fun findByUuid(companyUuid: UUID): Company? {
        return companyRepository.findByCompanyUuid(companyUuid).awaitFirstOrNull()?.withBoardMembers()
    }

    @Transactional
    override suspend fun updateByUuid(companyUuid: UUID, update: CompanyUpdate): Company {
        val company = companyRepository.findByCompanyUuid(companyUuid).awaitFirstOrNull()
            ?: throw OpexError.NotFound.exception("Company not found")

        update.registrationNumber?.takeIf { it != company.registrationNumber }?.let { registrationNumber ->
            companyRepository.findByRegistrationNumber(registrationNumber).awaitFirstOrNull()?.let {
                throw OpexError.CompanyAlreadyRegistered.exception("Registration number belongs to another company")
            }
            company.registrationNumber = registrationNumber
        }
        update.nationalId?.takeIf { it != company.nationalId }?.let { nationalId ->
            companyRepository.findByNationalId(nationalId).awaitFirstOrNull()?.let {
                throw OpexError.CompanyAlreadyRegistered.exception("National id belongs to another company")
            }
            company.nationalId = nationalId
        }
        update.name?.let { company.name = it }
        update.representativeNationalCardKey?.let { company.representativeNationalCardKey = it }
        update.introductionLetterKey?.let { company.introductionLetterKey = it }
        update.articlesOfAssociationKey?.let { company.articlesOfAssociationKey = it }
        update.officialNewspaperKey?.let { company.officialNewspaperKey = it }
        company.updateDate = LocalDateTime.now()

        val saved = companyRepository.save(company).awaitFirstOrNull()
            ?: throw OpexError.BadRequest.exception("Failed to update company")

        update.boardMemberDocumentKeys?.let { keys ->
            boardMemberDocumentRepository.deleteByCompanyId(saved.id!!).awaitFirstOrNull()
            keys.forEach { key ->
                boardMemberDocumentRepository.save(
                    CompanyBoardMemberDocumentModel().apply {
                        this.companyId = saved.id!!
                        s3Key = key
                        createDate = LocalDateTime.now()
                    }
                ).awaitFirstOrNull() ?: throw OpexError.BadRequest.exception("Failed to save board member document")
            }
        }

        return saved.withBoardMembers()
    }

    private suspend fun CompanyModel.withBoardMembers(): Company {
        val boardMemberKeys = boardMemberDocumentRepository.findByCompanyId(id!!)
            .collectList()
            .awaitFirstOrElse { emptyList() }
            .map { it.s3Key }
        return toCompany(boardMemberKeys)
    }

    private suspend fun saveCompanyAndLink(company: Company, representativeUuid: String): CompanyModel {
        companyRepresentativeRepository.findByUuid(representativeUuid).awaitFirstOrNull()?.let {
            throw OpexError.BadRequest.exception("Profile is already a representative of a company")
        }

        companyRepository
            .findByRegistrationNumberOrNationalId(company.registrationNumber, company.nationalId)
            .awaitFirstOrNull()
            ?.let { found ->
                if (found.registrationNumber != company.registrationNumber ||
                    found.nationalId != company.nationalId
                ) throw OpexError.CompanyIdentifiersMismatch.exception()
                throw OpexError.CompanyAlreadyRegistered.exception()
            }

        val saved = companyRepository.save(
            CompanyModel().apply {
                companyUuid = company.companyUuid
                name = company.name
                registrationNumber = company.registrationNumber
                nationalId = company.nationalId
                representativeNationalCardKey = company.representativeNationalCardKey
                introductionLetterKey = company.introductionLetterKey
                articlesOfAssociationKey = company.articlesOfAssociationKey
                officialNewspaperKey = company.officialNewspaperKey
                createDate = LocalDateTime.now()
                creator = company.creator
            }
        ).awaitFirstOrNull() ?: throw OpexError.BadRequest.exception("Failed to save company")

        companyRepresentativeRepository.save(
            CompanyRepresentativeModel().apply {
                this.companyId = saved.id!!
                uuid = representativeUuid
                createDate = LocalDateTime.now()
                creator = company.creator
            }
        ).awaitFirstOrNull() ?: throw OpexError.BadRequest.exception("Failed to save company representative")

        return saved
    }

    private suspend fun upsertRepresentativeProfile(registration: LegalAccountRegistration, creator: String) {
        profileRepository.upsertLegalRepresentative(
            userId = registration.uuid,
            email = registration.email,
            mobile = registration.mobile,
            firstName = registration.firstName,
            lastName = registration.lastName,
            identifier = registration.nationalCode,
            personType = PersonType.LEGAL.name,
            kycLevel = KycLevel.LEVEL_2.name,
            status = ProfileStatus.ADMIN_APPROVED.name,
            creator = creator
        ).awaitFirstOrNull()
    }

    private fun CompanyModel.toCompany(boardMemberKeys: List<String>) = Company(
        id = id,
        companyUuid = companyUuid,
        name = name,
        registrationNumber = registrationNumber,
        nationalId = nationalId,
        representativeNationalCardKey = representativeNationalCardKey,
        introductionLetterKey = introductionLetterKey,
        articlesOfAssociationKey = articlesOfAssociationKey,
        officialNewspaperKey = officialNewspaperKey,
        boardMemberDocumentKeys = boardMemberKeys,
        createDate = createDate,
        updateDate = updateDate,
        creator = creator
    )
}
