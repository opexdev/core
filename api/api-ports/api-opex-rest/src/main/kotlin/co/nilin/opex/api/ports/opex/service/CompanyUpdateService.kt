package co.nilin.opex.api.ports.opex.service

import co.nilin.opex.api.core.inout.CompanyAdminResponse
import co.nilin.opex.api.core.inout.UpdateCompanyRequest
import co.nilin.opex.api.core.spi.ProfileProxy
import co.nilin.opex.common.OpexError
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CompanyUpdateService(
    private val documentStorage: CompanyDocumentStorage,
    private val profileProxy: ProfileProxy
) {

    data class CompanyDetails(
        val name: String?,
        val registrationNumber: String?,
        val nationalId: String?
    )

    suspend fun update(
        token: String,
        companyUuid: UUID,
        details: CompanyDetails,
        documents: CompanyDocumentStorage.CompanyDocuments
    ): CompanyAdminResponse {
        val name = details.name?.trim()
        val registrationNumber = details.registrationNumber?.trim()
        val nationalId = details.nationalId?.trim()

        val blank = buildList {
            if (name?.isEmpty() == true) add("name")
            if (registrationNumber?.isEmpty() == true) add("registrationNumber")
            if (nationalId?.isEmpty() == true) add("nationalId")
        }
        if (blank.isNotEmpty())
            throw OpexError.InvalidRequestBody.exception("Fields must not be blank: ${blank.joinToString()}")

        val prepared = documentStorage.prepare(documents)
        if (name == null && registrationNumber == null && nationalId == null && prepared.isEmpty())
            throw OpexError.InvalidRequestBody.exception("At least one field or document must be provided")

        // Replaced files are never deleted: each update writes into a new version folder and the old ones stay in storage
        val uploaded = if (prepared.isEmpty()) null else {
            val current = profileProxy.getCompanyByUuid(token, companyUuid)
            documentStorage.upload(
                token,
                companyUuid,
                documentStorage.nextVersion(current),
                prepared
            )
        }

        return try {
            profileProxy.updateCompany(
                token,
                companyUuid,
                UpdateCompanyRequest(
                    name = name,
                    registrationNumber = registrationNumber,
                    nationalId = nationalId,
                    representativeNationalCardKey = uploaded?.representativeNationalCardKey,
                    introductionLetterKey = uploaded?.introductionLetterKey,
                    articlesOfAssociationKey = uploaded?.articlesOfAssociationKey,
                    officialNewspaperKey = uploaded?.officialNewspaperKey,
                    boardMemberDocumentKeys = uploaded?.boardMemberDocumentKeys
                )
            )
        } catch (e: Exception) {
            uploaded?.let { documentStorage.delete(token, it.allKeys) }
            throw e
        }
    }
}
