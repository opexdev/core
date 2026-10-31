package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.CardInfoInquiryRequest
import co.nilin.opex.api.core.inout.CardOwnershipInquiryRequest
import co.nilin.opex.api.core.inout.IbanInfoInquiryRequest
import co.nilin.opex.api.core.inout.IbanInfoInquiryResponse
import co.nilin.opex.api.core.inout.IbanOwnershipInquiryRequest
import co.nilin.opex.api.core.inout.IdentitySimilarityInquiryRequest
import co.nilin.opex.api.core.inout.IdentitySimilarityInquiryResponse
import co.nilin.opex.api.core.inout.ShahkarInquiryRequest
import co.nilin.opex.api.core.inout.ShahkarInquiryResponse
import co.nilin.opex.api.core.inout.VerifyOwnershipInquiryResponse
import co.nilin.opex.api.core.spi.ProfileProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private const val SHARED_NOTES = """
Behavior: Calls the identity inquiry provider directly and returns its answer untouched. `code` and `message` are the provider's own, so an error code the platform does not map yet can be read here. Nothing is validated for format and nothing is stored or decided: this does not verify a profile or a bank account, it only reports what the provider says.
Note: Every call is a paid inquiry at the provider and is logged with the acting admin.
Errors: 503 with code 13058 means the provider did not answer at all (timeout, provider down). Any other failure is reported in the `code` field of a 200 response."""

@RestController
@RequestMapping("/opex/v1/admin/inquiry")
@Tag(
    name = "Inquiry Admin",
    description = "Run an identity inquiry by hand and see the provider's raw answer."
)
class InquiryAdminController(
    private val profileProxy: ProfileProxy
) {

    @PostMapping("/shahkar", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Run a shahkar inquiry",
        description = """POST /opex/v1/admin/inquiry/shahkar.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Validation: `nationalCode` and `mobile` are required.$SHARED_NOTES
Answer: `matched` tells whether the national code and the mobile number belong to each other.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ShahkarInquiryResponse::class)
                )]
            ),
            ApiResponse(responseCode = "400", description = "A required field is missing.", content = [Content()]),
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
            ApiResponse(responseCode = "503", description = "The provider did not answer.", content = [Content()])
        ]
    )
    suspend fun shahkar(
        @RequestBody request: ShahkarInquiryRequest,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): ShahkarInquiryResponse {
        return profileProxy.shahkarInquiry(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @PostMapping("/identity-similarity", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Run an identity similarity inquiry",
        description = """POST /opex/v1/admin/inquiry/identity-similarity.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Validation: `nationalCode`, `firstName` and `lastName` are required. `birthDate` is epoch millis.$SHARED_NOTES
Answer: `firstNameSimilarityPercentage` and `lastNameSimilarityPercentage` are 0 to 100. The profile flow requires at least 95 for both.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = IdentitySimilarityInquiryResponse::class)
                )]
            ),
            ApiResponse(responseCode = "400", description = "A required field is missing.", content = [Content()]),
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
            ApiResponse(responseCode = "503", description = "The provider did not answer.", content = [Content()])
        ]
    )
    suspend fun identitySimilarity(
        @RequestBody request: IdentitySimilarityInquiryRequest,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): IdentitySimilarityInquiryResponse {
        return profileProxy.identitySimilarityInquiry(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @PostMapping("/card-ownership", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Run a card ownership inquiry",
        description = """POST /opex/v1/admin/inquiry/card-ownership.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Validation: `cardNumber` and `nationalCode` are required. `birthDate` is epoch millis.$SHARED_NOTES
Answer: `matched` tells whether the card belongs to the given national code and birth date.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = VerifyOwnershipInquiryResponse::class)
                )]
            ),
            ApiResponse(responseCode = "400", description = "A required field is missing.", content = [Content()]),
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
            ApiResponse(responseCode = "503", description = "The provider did not answer.", content = [Content()])
        ]
    )
    suspend fun cardOwnership(
        @RequestBody request: CardOwnershipInquiryRequest,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): VerifyOwnershipInquiryResponse {
        return profileProxy.cardOwnershipInquiry(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @PostMapping("/iban-ownership", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Run an iban ownership inquiry",
        description = """POST /opex/v1/admin/inquiry/iban-ownership.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Validation: `iban` and `nationalCode` are required. `birthDate` is epoch millis.$SHARED_NOTES
Answer: `matched` tells whether the iban belongs to the given national code and birth date.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = VerifyOwnershipInquiryResponse::class)
                )]
            ),
            ApiResponse(responseCode = "400", description = "A required field is missing.", content = [Content()]),
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
            ApiResponse(responseCode = "503", description = "The provider did not answer.", content = [Content()])
        ]
    )
    suspend fun ibanOwnership(
        @RequestBody request: IbanOwnershipInquiryRequest,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): VerifyOwnershipInquiryResponse {
        return profileProxy.ibanOwnershipInquiry(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @PostMapping("/card-info", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Look up the iban of a card",
        description = """POST /opex/v1/admin/inquiry/card-info.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Validation: `cardNumber` is required.$SHARED_NOTES
Answer: `ibanInfo` carries the bank, the deposit number and the iban behind the card. This is the inquiry that fills `accountNumber` and `bank` on a bank account.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = IbanInfoInquiryResponse::class)
                )]
            ),
            ApiResponse(responseCode = "400", description = "A required field is missing.", content = [Content()]),
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
            ApiResponse(responseCode = "503", description = "The provider did not answer.", content = [Content()])
        ]
    )
    suspend fun cardInfo(
        @RequestBody request: CardInfoInquiryRequest,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): IbanInfoInquiryResponse {
        return profileProxy.cardInfoInquiry(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @PostMapping("/iban-info", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Look up the details of an iban",
        description = """POST /opex/v1/admin/inquiry/iban-info.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Validation: `iban` is required.$SHARED_NOTES
Answer: `ibanInfo` carries the bank and the deposit number behind the iban.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = IbanInfoInquiryResponse::class)
                )]
            ),
            ApiResponse(responseCode = "400", description = "A required field is missing.", content = [Content()]),
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
            ApiResponse(responseCode = "503", description = "The provider did not answer.", content = [Content()])
        ]
    )
    suspend fun ibanInfo(
        @RequestBody request: IbanInfoInquiryRequest,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): IbanInfoInquiryResponse {
        return profileProxy.ibanInfoInquiry(securityContext.jwtAuthentication().tokenValue(), request)
    }
}
