package co.nilin.opex.profile.ports.postgres.imp

import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.core.data.kyc.KycLevel
import co.nilin.opex.profile.core.data.profile.Company
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
                    companyId = saved.id!!
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
                companyId = saved.id!!
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
