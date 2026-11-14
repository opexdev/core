package co.nilin.opex.api.ports.opex.service

import co.nilin.opex.api.core.inout.LegalAccountAvailabilityRequest
import co.nilin.opex.api.core.inout.LegalAccountRegistrationResponse
import co.nilin.opex.api.core.inout.RegisterLegalAccountRequest
import co.nilin.opex.api.core.inout.auth.CreateLegalRepresentativeRequest
import co.nilin.opex.api.core.spi.AuthProxy
import co.nilin.opex.api.core.spi.ProfileProxy
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.LoggerDelegate
import java.util.UUID
import org.springframework.stereotype.Service

@Service
class LegalAccountRegistrationService(
    private val documentStorage: CompanyDocumentStorage,
    private val authProxy: AuthProxy,
    private val profileProxy: ProfileProxy
) {
    private val logger by LoggerDelegate()

    data class LegalAccountDetails(
        val firstName: String,
        val lastName: String,
        val nationalCode: String,
        val email: String,
        val mobile: String,
        val companyName: String,
        val companyRegistrationNumber: String,
        val companyNationalId: String
    )

    suspend fun register(
        token: String,
        details: LegalAccountDetails,
        documents: CompanyDocumentStorage.CompanyDocuments
    ): LegalAccountRegistrationResponse {
        val companyUuid = UUID.randomUUID()
        validate(details)

        profileProxy.ensureLegalAccountAvailable(
            token,
            LegalAccountAvailabilityRequest(
                nationalCode = details.nationalCode,
                email = details.email,
                mobile = details.mobile,
                companyRegistrationNumber = details.companyRegistrationNumber,
                companyNationalId = details.companyNationalId
            )
        )

        val prepared = documentStorage.prepare(documents)
        val uploaded = documentStorage.upload(
            token, companyUuid, CompanyDocumentStorage.INITIAL_VERSION, prepared
        )

        val uuid = try {
            authProxy.createLegalRepresentative(
                CreateLegalRepresentativeRequest(
                    firstName = details.firstName,
                    lastName = details.lastName,
                    email = details.email,
                    mobile = details.mobile
                ),
                token
            ).uuid
        } catch (e: Exception) {
            documentStorage.delete(token, uploaded.allKeys)
            throw e
        }

        val company = try {
            profileProxy.registerLegalAccount(
                token,
                RegisterLegalAccountRequest(
                    companyUuid = companyUuid,
                    uuid = uuid,
                    firstName = details.firstName,
                    lastName = details.lastName,
                    nationalCode = details.nationalCode,
                    email = details.email,
                    mobile = details.mobile,
                    companyName = details.companyName,
                    companyRegistrationNumber = details.companyRegistrationNumber,
                    companyNationalId = details.companyNationalId,
                    representativeNationalCardKey = uploaded.representativeNationalCardKey,
                    introductionLetterKey = uploaded.introductionLetterKey,
                    articlesOfAssociationKey = uploaded.articlesOfAssociationKey,
                    officialNewspaperKey = uploaded.officialNewspaperKey,
                    boardMemberDocumentKeys = uploaded.boardMemberDocumentKeys ?: emptyList()
                )
            )
        } catch (e: Exception) {
            deleteUser(token, uuid)
            documentStorage.delete(token, uploaded.allKeys)
            throw e
        }

        val passwordEmailSent = authProxy.sendRepresentativeCredentials(uuid, token).emailSent

        return LegalAccountRegistrationResponse(uuid, company, passwordEmailSent)
    }

    private fun validate(details: LegalAccountDetails) {
        val missing = buildList {
            if (details.firstName.isBlank()) add("firstName")
            if (details.lastName.isBlank()) add("lastName")
            if (details.nationalCode.isBlank()) add("nationalCode")
            if (details.email.isBlank()) add("email")
            if (details.mobile.isBlank()) add("mobile")
            if (details.companyName.isBlank()) add("companyName")
            if (details.companyRegistrationNumber.isBlank()) add("companyRegistrationNumber")
            if (details.companyNationalId.isBlank()) add("companyNationalId")
        }
        if (missing.isNotEmpty())
            throw OpexError.InvalidRequestBody.exception("Missing required fields: ${missing.joinToString()}")
    }

    private suspend fun deleteUser(token: String, uuid: String) {
        runCatching { authProxy.deleteUser(uuid, token) }
            .onFailure { logger.error("Could not roll back representative $uuid", it) }
    }
}
