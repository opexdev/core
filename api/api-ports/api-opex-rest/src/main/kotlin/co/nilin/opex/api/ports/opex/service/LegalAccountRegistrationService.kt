package co.nilin.opex.api.ports.opex.service

import co.nilin.opex.api.core.inout.LegalAccountAvailabilityRequest
import co.nilin.opex.api.core.inout.LegalAccountRegistrationResponse
import co.nilin.opex.api.core.inout.RegisterLegalAccountRequest
import co.nilin.opex.api.core.inout.auth.CreateLegalRepresentativeRequest
import co.nilin.opex.api.core.spi.AuthProxy
import co.nilin.opex.api.core.spi.ProfileProxy
import co.nilin.opex.api.core.spi.StorageProxy
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class LegalAccountRegistrationService(
    private val storageProxy: StorageProxy,
    private val authProxy: AuthProxy,
    private val profileProxy: ProfileProxy,
    @Value("\${app.company.documents-bucket}")
    private val documentsBucket: String
) {
    private val logger by LoggerDelegate()

    data class LegalAccountDocuments(
        val representativeNationalCard: FilePart?,
        val introductionLetter: FilePart?,
        val articlesOfAssociation: FilePart?,
        val officialNewspaper: FilePart?,
        val boardMemberNationalCards: Flux<FilePart>?
    )

    data class LegalAccountDetails(
        val firstName: String,
        val lastName: String,
        val nationalCode: String,
        val email: String,
        val mobile: String,
        val companyRegistrationNumber: String,
        val companyNationalId: String
    )

    suspend fun register(
        token: String,
        details: LegalAccountDetails,
        documents: LegalAccountDocuments
    ): LegalAccountRegistrationResponse {
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

        val boardMemberCards = documents.boardMemberNationalCards?.asFlow()?.toList() ?: emptyList()
        val namedDocuments = listOfNotNull(
            documents.representativeNationalCard?.let { REPRESENTATIVE_NATIONAL_CARD to it },
            documents.introductionLetter?.let { INTRODUCTION_LETTER to it },
            documents.articlesOfAssociation?.let { ARTICLES_OF_ASSOCIATION to it },
            documents.officialNewspaper?.let { OFFICIAL_NEWSPAPER to it }
        )
        val indexedBoardMemberCards = boardMemberCards.mapIndexed { index, file -> "board-member-${index + 1}" to file }
        (namedDocuments + indexedBoardMemberCards).forEach { (_, file) -> validateExtension(file) }

        val uploadedKeys = mutableListOf<String>()

        val keyByName = try {
            (namedDocuments + indexedBoardMemberCards).associate { (name, file) ->
                val key = documentKey(details.companyNationalId, name, file)
                storageProxy.adminUpload(token, documentsBucket, key, file, false)
                uploadedKeys += key
                name to key
            }
        } catch (e: Exception) {
            deleteDocuments(token, uploadedKeys)
            throw e
        }

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
            deleteDocuments(token, uploadedKeys)
            throw e
        }

        val company = try {
            profileProxy.registerLegalAccount(
                token,
                RegisterLegalAccountRequest(
                    uuid = uuid,
                    firstName = details.firstName,
                    lastName = details.lastName,
                    nationalCode = details.nationalCode,
                    email = details.email,
                    mobile = details.mobile,
                    companyRegistrationNumber = details.companyRegistrationNumber,
                    companyNationalId = details.companyNationalId,
                    representativeNationalCardKey = keyByName[REPRESENTATIVE_NATIONAL_CARD],
                    introductionLetterKey = keyByName[INTRODUCTION_LETTER],
                    articlesOfAssociationKey = keyByName[ARTICLES_OF_ASSOCIATION],
                    officialNewspaperKey = keyByName[OFFICIAL_NEWSPAPER],
                    boardMemberDocumentKeys = indexedBoardMemberCards.mapNotNull { (name, _) -> keyByName[name] }
                )
            )
        } catch (e: Exception) {
            deleteUser(token, uuid)
            deleteDocuments(token, uploadedKeys)
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
            if (details.companyRegistrationNumber.isBlank()) add("companyRegistrationNumber")
            if (details.companyNationalId.isBlank()) add("companyNationalId")
        }
        if (missing.isNotEmpty())
            throw OpexError.InvalidRequestBody.exception("Missing required fields: ${missing.joinToString()}")
    }

    private fun validateExtension(file: FilePart) {
        val extension = file.filename().substringAfterLast('.', "").lowercase()
        if (extension !in ALLOWED_EXTENSIONS)
            throw OpexError.InvalidRequestBody.exception(
                "Unsupported file format '${file.filename()}'. Allowed: ${ALLOWED_EXTENSIONS.joinToString()}"
            )
    }

    private fun documentKey(companyNationalId: String, name: String, file: FilePart): String {
        val extension = file.filename().substringAfterLast('.', "").lowercase()
        return "company/$companyNationalId/$name.$extension"
    }

    private suspend fun deleteDocuments(token: String, keys: List<String>) {
        keys.forEach { key ->
            runCatching { storageProxy.adminDelete(token, documentsBucket, key) }
                .onFailure { logger.error("Could not roll back document $key", it) }
        }
    }

    private suspend fun deleteUser(token: String, uuid: String) {
        runCatching { authProxy.deleteUser(uuid, token) }
            .onFailure { logger.error("Could not roll back representative $uuid", it) }
    }

    private companion object {
        const val REPRESENTATIVE_NATIONAL_CARD = "representative-national-card"
        const val INTRODUCTION_LETTER = "introduction-letter"
        const val ARTICLES_OF_ASSOCIATION = "articles-of-association"
        const val OFFICIAL_NEWSPAPER = "official-newspaper"
        val ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "svg")
    }
}
