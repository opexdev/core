package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.CheckoutStatus
import co.nilin.opex.api.core.inout.RewardClaimCheckoutErrorDto
import co.nilin.opex.api.core.inout.RewardClaimDto
import co.nilin.opex.api.core.inout.UpdateRewardClaimStatusRequest
import co.nilin.opex.api.core.spi.ReferralProxy
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/opex/v1/admin/referral")
@Tag(name = "Referral Admin", description = "Admin referral reward claim operations.")
class ReferralAdminController(private val referralProxy: ReferralProxy) {

    @GetMapping("/reward-claims", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "List reward claims",
        description = """GET /opex/v1/admin/referral/reward-claims.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Behavior: Returns reward claims across all users, optionally filtered by checkout status and/or rewarded user uuid, paginated by offset/limit.
Allowed values:
- status: PENDING, SUCCESS, FAILED.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = RewardClaimDto::class))
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
    suspend fun getRewardClaims(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "status", description = "Filter by checkout status. Allowed values: PENDING, SUCCESS, FAILED.", required = false)
        @RequestParam(required = false) status: CheckoutStatus?,
        @Parameter(name = "uuid", description = "Filter by rewarded user uuid.", required = false)
        @RequestParam(required = false) uuid: String?,
        @Parameter(name = "offset", description = "Pagination offset.", required = false)
        @RequestParam(defaultValue = "0") offset: Int,
        @Parameter(name = "limit", description = "Page size.", required = false)
        @RequestParam(defaultValue = "50") limit: Int
    ): List<RewardClaimDto> {
        return referralProxy.getRewardClaims(
            securityContext.jwtAuthentication().tokenValue(),
            status,
            uuid,
            offset,
            limit
        )
    }

    @GetMapping("/reward-claims/{id}/errors", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "List reward claim errors",
        description = """GET /opex/v1/admin/referral/reward-claims/{id}/errors.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Behavior: Returns the checkout errors recorded for the given reward claim.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = RewardClaimCheckoutErrorDto::class))
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
    suspend fun getRewardClaimErrors(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "id", description = "Reward claim id.", required = true)
        @PathVariable id: Long
    ): List<RewardClaimCheckoutErrorDto> {
        return referralProxy.getRewardClaimErrors(securityContext.jwtAuthentication().tokenValue(), id)
    }

    @PostMapping("/reward-claims/{id}/retry", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Retry reward claim",
        description = """POST /opex/v1/admin/referral/reward-claims/{id}/retry.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Behavior: Re-runs checkout for the given reward claim and returns its updated state.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = RewardClaimDto::class))]
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
    suspend fun retryRewardClaim(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "id", description = "Reward claim id.", required = true)
        @PathVariable id: Long
    ): RewardClaimDto {
        return referralProxy.retryRewardClaim(securityContext.jwtAuthentication().tokenValue(), id)
    }

    @PutMapping("/reward-claims/{id}/status", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Update reward claim status",
        description = """PUT /opex/v1/admin/referral/reward-claims/{id}/status.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Validation: `status` is required.
Behavior: Overrides the checkout status of the given reward claim.
Allowed values:
- status: PENDING, SUCCESS, FAILED.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = RewardClaimDto::class))]
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
    suspend fun updateRewardClaimStatus(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "id", description = "Reward claim id.", required = true)
        @PathVariable id: Long,
        @RequestBody request: UpdateRewardClaimStatusRequest
    ): RewardClaimDto {
        return referralProxy.updateRewardClaimStatus(
            securityContext.jwtAuthentication().tokenValue(),
            id,
            request.status
        )
    }
}
