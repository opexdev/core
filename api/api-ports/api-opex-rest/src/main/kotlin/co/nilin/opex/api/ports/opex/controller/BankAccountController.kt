package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.AddBankAccountRequest
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

Validation: Exactly one of `cardNumber` or `iban` must be provided. Providing both or neither is invalid.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Response body: See schema.", content = [Content(mediaType = "application/json", schema = Schema(implementation = BankAccountResponse::class))]),
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
Security: Bearer user-token required. Requires authenticated user JWT.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Response body: See schema.", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = BankAccountResponse::class)))]),
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
Security: Bearer user-token required. Required authority: PERM_bank_account:write.""",
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
}
