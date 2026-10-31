package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.AddBankAccountRequest
import co.nilin.opex.api.core.inout.BankAccountApprovalUserResponse
import co.nilin.opex.api.core.inout.BankAccountResponse
import co.nilin.opex.api.core.spi.ProfileProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag

@RestController
@RequestMapping("/opex/v1/bank-account")
@Tag(name = "Bank Account", description = "Manage authenticated user's bank accounts.")
class BankAccountController(
    val profileProxy: ProfileProxy
) {

    @PostMapping
    @Operation(
        summary = "Add bank account",
        description = """POST /opex/v1/bank-account.
Security: Bearer user-token required. Requires authenticated user JWT.

Validation: Exactly one of `cardNumber` or `iban` must be provided. Providing both or neither is invalid.
Behavior: How the account is verified depends on the deployment. With automatic verification on, the card/iban identity inquiry decides: a match returns VERIFIED, and an answer that rejects the user's data fails the request. With automatic verification off, the account is stored as WAITING for an admin. An inquiry the provider could not answer (timeout, provider down) returns WAITING when admin review is enabled, and fails the request otherwise. Follow a WAITING account with GET /opex/v1/bank-account/{id}/approval-request.
Allowed values:
- status: WAITING, VERIFIED, REJECTED.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = BankAccountResponse::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()])
        ]
    )
    suspend fun addBankAccount(
        @RequestBody request: AddBankAccountRequest,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): BankAccountResponse {
        return profileProxy.addBankAccount(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @GetMapping
    @Operation(
        summary = "Get bank accounts",
        description = """GET /opex/v1/bank-account.
Security: Bearer user-token required. Requires authenticated user JWT.
Allowed values:
- status: WAITING, VERIFIED, REJECTED.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = BankAccountResponse::class)))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()])
        ]
    )
    suspend fun getBankAccounts(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): List<BankAccountResponse> {
        return profileProxy.getBankAccounts(securityContext.jwtAuthentication().tokenValue())
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete bank account",
        description = """DELETE /opex/v1/bank-account/{id}.
Security: Bearer user-token required. Required authority: PERM_bank_account:write.
Allowed values:
- status: WAITING, VERIFIED, REJECTED.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "No response body.", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: PERM_bank_account:write. No response body.", content = [Content()])
        ]
    )
    suspend fun deleteBankAccount(
        @PathVariable("id") id: Long,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        profileProxy.deleteBankAccount(securityContext.jwtAuthentication().tokenValue(), id)
    }

    @GetMapping("/{id}/approval-request")
    @Operation(
        summary = "Get the review of a bank account",
        description = """GET /opex/v1/bank-account/{id}/approval-request.
Security: Bearer user-token required. Requires authenticated user JWT.
Behavior: Returns the admin review of the caller's own bank account, including the admin's note once a decision is made. A bank account only has one when its identity inquiry could not be completed. Reading another user's bank account is forbidden.
Allowed values:
- status: WAITING, VERIFIED, REJECTED. This is the bank account's own status, so WAITING means the review is still open.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = BankAccountApprovalUserResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. The bank account belongs to another user. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "404",
                description = "The bank account does not exist or has no review.",
                content = [Content()]
            )
        ]
    )
    suspend fun getApprovalRequest(
        @Parameter(name = "id", description = "Bank account id.", required = true)
        @PathVariable("id") id: Long,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): BankAccountApprovalUserResponse {
        return profileProxy.getUserBankAccountApprovalRequest(securityContext.jwtAuthentication().tokenValue(), id)
    }
}
