package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.CompanyAdminResponse
import co.nilin.opex.api.core.inout.LegalAccountRegistrationResponse
import co.nilin.opex.api.core.spi.ProfileProxy
import co.nilin.opex.api.core.spi.StorageProxy
import co.nilin.opex.api.ports.opex.service.CompanyDocumentStorage
import co.nilin.opex.api.ports.opex.service.CompanyUpdateService
import co.nilin.opex.api.ports.opex.service.LegalAccountRegistrationService
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import co.nilin.opex.common.OpexError
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.util.UUID

@RestController
@RequestMapping("/opex/v1/admin/company")
@Tag(name = "Company Admin", description = "Admin company registration operations.")
class CompanyAdminController(
    private val profileProxy: ProfileProxy,
    private val storageProxy: StorageProxy,
    private val legalAccountRegistrationService: LegalAccountRegistrationService,
    private val companyUpdateService: CompanyUpdateService,
    @Value("\${app.company.documents-bucket}")
    private val documentsBucket: String
) {

    @PostMapping(
        "/legal-account",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(
        summary = "Register legal account",
        description = """POST /opex/v1/admin/company/legal-account.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Validation: `firstName`, `lastName`, `nationalCode`, `email`, `mobile`, `companyName`, `companyRegistrationNumber` and `companyNationalId` are required. Every document part is optional. Allowed document formats: jpg, jpeg, png, webp, svg, pdf. Each file is capped at 5MB by the storage service.
Behavior: Multipart registration of a whole corporate account. Generates the company UUID, uploads the documents under `company/{companyUuid}/v1/`, creates the representative's Keycloak user, persists the profile and the company in one transaction, raises the user to level 2, then emails a generated password to the representative.
Response: The representative `uuid`, the company with its document keys, and `passwordEmailSent`. A false `passwordEmailSent` means the account is usable but the password was not delivered — resend the credentials rather than retrying the registration.
Rollback: A failure before the password email removes the Keycloak user and the uploaded documents, and leaves no database rows behind.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = LegalAccountRegistrationResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "A required field is missing, a document format is unsupported, or the company identifiers are already taken.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required authority is missing: ROLE_admin. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "409",
                description = "A user with this email already exists.",
                content = [Content()]
            )
        ]
    )
    suspend fun registerLegalAccount(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "firstName", description = "Representative first name.", required = true)
        @RequestPart("firstName") firstName: String,
        @Parameter(name = "lastName", description = "Representative last name.", required = true)
        @RequestPart("lastName") lastName: String,
        @Parameter(name = "nationalCode", description = "Representative national code.", required = true)
        @RequestPart("nationalCode") nationalCode: String,
        @Parameter(name = "email", description = "Representative email. The generated password is sent here.", required = true)
        @RequestPart("email") email: String,
        @Parameter(name = "mobile", description = "Representative mobile number.", required = true)
        @RequestPart("mobile") mobile: String,
        @Parameter(name = "companyName", description = "Company name.", required = true)
        @RequestPart("companyName") companyName: String,
        @Parameter(name = "companyRegistrationNumber", description = "Company registration number.", required = true)
        @RequestPart("companyRegistrationNumber") companyRegistrationNumber: String,
        @Parameter(name = "companyNationalId", description = "Company national id.", required = true)
        @RequestPart("companyNationalId") companyNationalId: String,
        @Parameter(name = "representativeNationalCard", description = "Representative national card image.", required = false)
        @RequestPart(value = "representativeNationalCard", required = false) representativeNationalCard: FilePart?,
        @Parameter(name = "introductionLetter", description = "Introduction letter image.", required = false)
        @RequestPart(value = "introductionLetter", required = false) introductionLetter: FilePart?,
        @Parameter(name = "articlesOfAssociation", description = "Articles of association image.", required = false)
        @RequestPart(value = "articlesOfAssociation", required = false) articlesOfAssociation: FilePart?,
        @Parameter(name = "officialNewspaper", description = "Official newspaper image.", required = false)
        @RequestPart(value = "officialNewspaper", required = false) officialNewspaper: FilePart?,
        @Parameter(name = "boardMemberNationalCards", description = "Board member national card images. Any number of files.", required = false)
        @RequestPart(value = "boardMemberNationalCards", required = false) boardMemberNationalCards: Flux<FilePart>?
    ): LegalAccountRegistrationResponse {
        return legalAccountRegistrationService.register(
            securityContext.jwtAuthentication().tokenValue(),
            LegalAccountRegistrationService.LegalAccountDetails(
                firstName = firstName,
                lastName = lastName,
                nationalCode = nationalCode,
                email = email,
                mobile = mobile,
                companyName = companyName,
                companyRegistrationNumber = companyRegistrationNumber,
                companyNationalId = companyNationalId
            ),
            CompanyDocumentStorage.CompanyDocuments(
                representativeNationalCard = representativeNationalCard,
                introductionLetter = introductionLetter,
                articlesOfAssociation = articlesOfAssociation,
                officialNewspaper = officialNewspaper,
                boardMemberNationalCards = boardMemberNationalCards
            )
        )
    }

    @GetMapping("/national-id/{nationalId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get company by national id",
        description = """GET /opex/v1/admin/company/national-id/{nationalId}.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Behavior: Returns the company with this national id, including the storage keys of its documents.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = CompanyAdminResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required authority is missing: ROLE_admin. No response body.",
                content = [Content()]
            ),
            ApiResponse(responseCode = "404", description = "No company has this national id.", content = [Content()])
        ]
    )
    suspend fun getCompanyByNationalId(
        @Parameter(name = "nationalId", description = "Company national id.", required = true)
        @PathVariable("nationalId") nationalId: String,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): CompanyAdminResponse {
        return profileProxy.getCompanyByNationalId(securityContext.jwtAuthentication().tokenValue(), nationalId)
    }

    @GetMapping("/{uuid}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get company of user",
        description = """GET /opex/v1/admin/company/{uuid}.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Behavior: Returns the company the given profile is a representative of, including the storage keys of its documents.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = CompanyAdminResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required authority is missing: ROLE_admin. No response body.",
                content = [Content()]
            ),
            ApiResponse(responseCode = "404", description = "Profile does not represent a company.", content = [Content()])
        ]
    )
    suspend fun getCompany(
        @Parameter(name = "uuid", description = "Profile uuid.", required = true)
        @PathVariable("uuid") uuid: String,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): CompanyAdminResponse {
        return profileProxy.getCompanyAdmin(securityContext.jwtAuthentication().tokenValue(), uuid)
    }

    @PutMapping(
        "/{companyUuid}",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(
        summary = "Update company",
        description = """PUT /opex/v1/admin/company/{companyUuid}.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Validation: At least one part must be sent. A sent text part must not be blank. Allowed document formats: jpg, jpeg, png, webp, svg, pdf. Each file is capped at 5MB by the storage service.
Behavior: Multipart update of the company identified by `companyUuid`. Every part is optional; an omitted part keeps its current value. `registrationNumber` and `nationalId` must not belong to another company.
Documents: A sent document replaces the current one. Sending any `boardMemberNationalCards` replaces the whole board member set. Each update that sends documents gets the next version folder, `company/{companyUuid}/v{n}/` (registration is v1), holding only the files sent in that update; the folder is independent of `nationalId`. Documents that are not sent keep their existing storage keys.
History: Replaced files are not deleted and stay in their version folders in storage. Only the current documents are returned and downloadable.
Rollback: If the update fails, the newly uploaded files are removed and the company and its current documents stay unchanged.
Response: The updated company, including the storage keys of its documents.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = CompanyAdminResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "No part was sent, a sent text part is blank, or a document format is unsupported.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required authority is missing: ROLE_admin. No response body.",
                content = [Content()]
            ),
            ApiResponse(responseCode = "404", description = "Company not found.", content = [Content()]),
            ApiResponse(
                responseCode = "409",
                description = "The registration number or national id belongs to another company.",
                content = [Content()]
            )
        ]
    )
    suspend fun updateCompany(
        @Parameter(name = "companyUuid", description = "Company UUID returned by the admin company lookup.", required = true)
        @PathVariable("companyUuid") companyUuid: UUID,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "name", description = "New company name.", required = false)
        @RequestPart(value = "name", required = false) name: String?,
        @Parameter(name = "registrationNumber", description = "New company registration number.", required = false)
        @RequestPart(value = "registrationNumber", required = false) registrationNumber: String?,
        @Parameter(name = "nationalId", description = "New company national id.", required = false)
        @RequestPart(value = "nationalId", required = false) nationalId: String?,
        @Parameter(name = "representativeNationalCard", description = "Replaces the representative national card image.", required = false)
        @RequestPart(value = "representativeNationalCard", required = false) representativeNationalCard: FilePart?,
        @Parameter(name = "introductionLetter", description = "Replaces the introduction letter image.", required = false)
        @RequestPart(value = "introductionLetter", required = false) introductionLetter: FilePart?,
        @Parameter(name = "articlesOfAssociation", description = "Replaces the articles of association image.", required = false)
        @RequestPart(value = "articlesOfAssociation", required = false) articlesOfAssociation: FilePart?,
        @Parameter(name = "officialNewspaper", description = "Replaces the official newspaper image.", required = false)
        @RequestPart(value = "officialNewspaper", required = false) officialNewspaper: FilePart?,
        @Parameter(name = "boardMemberNationalCards", description = "Replaces all board member national card images. Any number of files.", required = false)
        @RequestPart(value = "boardMemberNationalCards", required = false) boardMemberNationalCards: Flux<FilePart>?
    ): CompanyAdminResponse {
        return companyUpdateService.update(
            securityContext.jwtAuthentication().tokenValue(),
            companyUuid,
            CompanyUpdateService.CompanyDetails(name, registrationNumber, nationalId),
            CompanyDocumentStorage.CompanyDocuments(
                representativeNationalCard = representativeNationalCard,
                introductionLetter = introductionLetter,
                articlesOfAssociation = articlesOfAssociation,
                officialNewspaper = officialNewspaper,
                boardMemberNationalCards = boardMemberNationalCards
            )
        )
    }

    @GetMapping("/document")
    @Operation(
        summary = "Download company document",
        description = """GET /opex/v1/admin/company/document.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Validation: `key` must be one of the document keys of the company identified by `nationalId`.
Behavior: Streams the stored document. Documents are uploaded private, so this is the only way to read them.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/octet-stream",
                    schema = Schema(type = "string", format = "binary")
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required authority is missing: ROLE_admin. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "404",
                description = "No company has this national id, or the key does not belong to it.",
                content = [Content()]
            )
        ]
    )
    suspend fun downloadDocument(
        @Parameter(name = "nationalId", description = "Company national id.", required = true)
        @RequestParam("nationalId") nationalId: String,
        @Parameter(name = "key", description = "Storage key of one of the company documents.", required = true)
        @RequestParam("key") key: String,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): ResponseEntity<ByteArray> {
        val token = securityContext.jwtAuthentication().tokenValue()
        val company = profileProxy.getCompanyByNationalId(token, nationalId)

        val ownedKeys = listOfNotNull(
            company.representativeNationalCardKey,
            company.introductionLetterKey,
            company.articlesOfAssociationKey,
            company.officialNewspaperKey
        ) + company.boardMemberDocumentKeys
        if (key !in ownedKeys)
            throw OpexError.NotFound.exception("Document does not belong to this company")

        return storageProxy.adminDownload(token, documentsBucket, key)
    }
}
