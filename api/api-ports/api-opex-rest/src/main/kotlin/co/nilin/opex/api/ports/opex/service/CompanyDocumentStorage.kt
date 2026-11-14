package co.nilin.opex.api.ports.opex.service

import co.nilin.opex.api.core.inout.CompanyAdminResponse
import co.nilin.opex.api.core.spi.StorageProxy
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.util.UUID

@Component
class CompanyDocumentStorage(
    private val storageProxy: StorageProxy,
    @Value("\${app.company.documents-bucket}")
    private val documentsBucket: String
) {
    private val logger by LoggerDelegate()

    data class CompanyDocuments(
        val representativeNationalCard: FilePart?,
        val introductionLetter: FilePart?,
        val articlesOfAssociation: FilePart?,
        val officialNewspaper: FilePart?,
        val boardMemberNationalCards: Flux<FilePart>?
    )

    class PreparedDocuments internal constructor(
        internal val named: List<Pair<String, FilePart>>,
        internal val boardMembers: List<Pair<String, FilePart>>
    ) {
        fun isEmpty() = named.isEmpty() && boardMembers.isEmpty()
    }

    data class UploadedDocuments(
        val representativeNationalCardKey: String?,
        val introductionLetterKey: String?,
        val articlesOfAssociationKey: String?,
        val officialNewspaperKey: String?,
        val boardMemberDocumentKeys: List<String>?,
        val allKeys: List<String>
    )

    suspend fun prepare(documents: CompanyDocuments): PreparedDocuments {
        val named = listOfNotNull(
            documents.representativeNationalCard.ignoreIfEmpty()?.let { REPRESENTATIVE_NATIONAL_CARD to it },
            documents.introductionLetter.ignoreIfEmpty()?.let { INTRODUCTION_LETTER to it },
            documents.articlesOfAssociation.ignoreIfEmpty()?.let { ARTICLES_OF_ASSOCIATION to it },
            documents.officialNewspaper.ignoreIfEmpty()?.let { OFFICIAL_NEWSPAPER to it }
        )
        val boardMembers = (documents.boardMemberNationalCards?.asFlow()?.toList() ?: emptyList())
            .mapNotNull { it.ignoreIfEmpty() }
            .mapIndexed { index, file -> "board-member-${index + 1}" to file }
        (named + boardMembers).forEach { (_, file) -> validateExtension(file) }
        return PreparedDocuments(named, boardMembers)
    }

    private fun FilePart?.ignoreIfEmpty(): FilePart? = this?.takeIf { it.filename().isNotBlank() }

    suspend fun upload(
        token: String,
        companyUuid: UUID,
        version: Int,
        documents: PreparedDocuments
    ): UploadedDocuments {
        val uploadedKeys = mutableListOf<String>()
        val keyByName = try {
            (documents.named + documents.boardMembers).associate { (name, file) ->
                val key = documentKey(companyUuid, version, name, file)
                storageProxy.adminUpload(token, documentsBucket, key, file, false)
                uploadedKeys += key
                name to key
            }
        } catch (e: Exception) {
            delete(token, uploadedKeys)
            throw e
        }

        return UploadedDocuments(
            representativeNationalCardKey = keyByName[REPRESENTATIVE_NATIONAL_CARD],
            introductionLetterKey = keyByName[INTRODUCTION_LETTER],
            articlesOfAssociationKey = keyByName[ARTICLES_OF_ASSOCIATION],
            officialNewspaperKey = keyByName[OFFICIAL_NEWSPAPER],
            boardMemberDocumentKeys = documents.boardMembers
                .mapNotNull { (name, _) -> keyByName[name] }
                .takeIf { it.isNotEmpty() },
            allKeys = uploadedKeys
        )
    }

    fun nextVersion(company: CompanyAdminResponse): Int {
        val keys = listOfNotNull(
            company.representativeNationalCardKey,
            company.introductionLetterKey,
            company.articlesOfAssociationKey,
            company.officialNewspaperKey
        ) + company.boardMemberDocumentKeys
        val latest = keys.maxOfOrNull { versionOf(it) } ?: PRE_VERSIONING
        return latest + 1
    }

    private fun versionOf(key: String): Int =
        VERSION_FOLDER.find(key)?.groupValues?.get(1)?.toIntOrNull() ?: PRE_VERSIONING

    suspend fun delete(token: String, keys: Collection<String>) {
        keys.forEach { key ->
            runCatching { storageProxy.adminDelete(token, documentsBucket, key) }
                .onFailure { logger.error("Could not delete company document $key", it) }
        }
    }

    private fun validateExtension(file: FilePart) {
        val extension = file.filename().substringAfterLast('.', "").lowercase()
        if (extension !in ALLOWED_EXTENSIONS)
            throw OpexError.InvalidRequestBody.exception(
                "Unsupported file format '${file.filename()}'. Allowed: ${ALLOWED_EXTENSIONS.joinToString()}"
            )
    }

    private fun documentKey(companyUuid: UUID, version: Int, name: String, file: FilePart): String {
        val extension = file.filename().substringAfterLast('.', "").lowercase()
        return "company/$companyUuid/v$version/$name.$extension"
    }

    companion object {
        const val INITIAL_VERSION = 1

        private const val PRE_VERSIONING = 0
        private const val REPRESENTATIVE_NATIONAL_CARD = "representative-national-card"
        private const val INTRODUCTION_LETTER = "introduction-letter"
        private const val ARTICLES_OF_ASSOCIATION = "articles-of-association"
        private const val OFFICIAL_NEWSPAPER = "official-newspaper"
        private val ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "svg", "pdf")
        private val VERSION_FOLDER = Regex("/v(\\d+)/[^/]+$")
    }
}
