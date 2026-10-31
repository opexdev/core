package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.BankAccountApprovalAdminResponse
import co.nilin.opex.api.core.inout.BankAccountApprovalRequestFilter
import co.nilin.opex.api.core.inout.UpdateBankAccountApprovalBody
import co.nilin.opex.api.core.spi.ProfileProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/opex/v1/admin/bank-account")
@Tag(
    name = "Bank Account Admin",
    description = "Admin review of bank accounts whose identity inquiry could not be completed."
)
class BankAccountAdminController(
    private val profileProxy: ProfileProxy
) {

    @PostMapping("/approval-requests", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get bank account approval requests",
        description = """POST /opex/v1/admin/bank-account/approval-requests.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Behavior: Lists bank account review requests, newest first unless `ascendingByTime` is true. Requests exist only while admin approval is enabled: with automatic verification off every account creates one, and with it on only an inquiry the provider could not answer (timeout, provider down) does. A provider answer that rejects the user's data rejects the account instead of creating a request. Each row carries the account under review and the owner's name, so no second call is needed.
Filters: `userId`, `status`, `createDateFrom` and `createDateTo` (epoch millis), `limit`, `offset`, `ascendingByTime`. Every filter is optional.
Allowed values:
- status: WAITING, VERIFIED, REJECTED. This is the bank account's own status; a review has none of its own, so WAITING is a review still open.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = BankAccountApprovalAdminResponse::class))
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
            )
        ]
    )
    suspend fun getApprovalRequests(
        @RequestBody request: BankAccountApprovalRequestFilter,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): List<BankAccountApprovalAdminResponse> {
        return profileProxy.getBankAccountApprovalRequests(
            securityContext.jwtAuthentication().tokenValue(),
            request
        )
    }

    @GetMapping("/approval-request/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get bank account approval request",
        description = """GET /opex/v1/admin/bank-account/approval-request/{id}.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Behavior: Returns one review request with the account under review and the owner's first and last name.
Allowed values:
- status: WAITING, VERIFIED, REJECTED. This is the bank account's own status; a review has none of its own.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = BankAccountApprovalAdminResponse::class)
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
            ApiResponse(responseCode = "404", description = "No request with this id.", content = [Content()])
        ]
    )
    suspend fun getApprovalRequest(
        @Parameter(name = "id", description = "Approval request id.", required = true)
        @PathVariable("id") id: Long,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): BankAccountApprovalAdminResponse {
        return profileProxy.getBankAccountApprovalRequest(securityContext.jwtAuthentication().tokenValue(), id)
    }

    @PutMapping("/approval-request", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Update bank account approval request status",
        description = """PUT /opex/v1/admin/bank-account/approval-request.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Validation: `id` and `status` are required. `status` must be VERIFIED or REJECTED, and only an account still WAITING can be decided on.
Behavior: Closes the review by setting the bank account's status. The acting admin and the note are recorded on the request. A review has no status of its own, so this is the account's own status and there is nothing that could disagree with it. Only the status changes; the account data itself is never edited here.
Allowed values:
- status: VERIFIED, REJECTED.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = BankAccountApprovalAdminResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "The review is already closed, or the status is not VERIFIED or REJECTED, or the same card or iban was verified for another user in the meantime.",
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
            ApiResponse(responseCode = "404", description = "No request with this id.", content = [Content()])
        ]
    )
    suspend fun updateRequestStatus(
        @RequestBody request: UpdateBankAccountApprovalBody,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): BankAccountApprovalAdminResponse {
        return profileProxy.updateBankAccountApprovalRequest(
            securityContext.jwtAuthentication().tokenValue(),
            request
        )
    }
}
