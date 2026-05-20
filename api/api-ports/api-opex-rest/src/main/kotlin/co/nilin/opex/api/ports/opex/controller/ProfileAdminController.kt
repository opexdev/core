package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.*
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
@RequestMapping("/opex/v1/admin/profile")
@Tag(name = "Profile Admin", description = "Admin profile and approval request operations.")
class ProfileAdminController(private val profileProxy: ProfileProxy) {

    @PostMapping
    @Operation(
        summary = "Get profiles",
        description = """POST /opex/v1/admin/profile.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Allowed values:
- nationality: IRANIAN, NON_IRANIAN.
- gender: FEMALE, MALE.
- status: CREATED, CONTACT_INFO_COMPLETED, PROFILE_COMPLETED, SYSTEM_APPROVED, PENDING_ADMIN_APPROVAL, ADMIN_REJECTED, ADMIN_APPROVED.
- kycLevel: LEVEL_1, LEVEL_2, LEVEL_3.
- approval request status: PENDING, APPROVED, REJECTED.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = Profile::class))
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
    suspend fun getProfiles(
        @RequestBody profileRequest: ProfileRequest,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): List<Profile> {
        return profileProxy.getProfiles(
            securityContext.jwtAuthentication().tokenValue(),
            profileRequest
        )
    }

    @GetMapping("/{uuid}")
    @Operation(
        summary = "Get profile",
        description = """GET /opex/v1/admin/profile/{uuid}.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Allowed values:
- nationality: IRANIAN, NON_IRANIAN.
- gender: FEMALE, MALE.
- status: CREATED, CONTACT_INFO_COMPLETED, PROFILE_COMPLETED, SYSTEM_APPROVED, PENDING_ADMIN_APPROVAL, ADMIN_REJECTED, ADMIN_APPROVED.
- kycLevel: LEVEL_1, LEVEL_2, LEVEL_3.
- approval request status: PENDING, APPROVED, REJECTED.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = Profile::class))]
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
    suspend fun getProfile(
        @Parameter(
            name = "uuid",
            description = "User/profile/terminal UUID depending on the endpoint context.",
            required = true
        )
        @PathVariable uuid: String,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): Profile {
        return profileProxy.getProfileAdmin(securityContext.jwtAuthentication().tokenValue(), uuid)
    }

    @GetMapping("/history/{uuid}")
    @Operation(
        summary = "Get profile history",
        description = """GET /opex/v1/admin/profile/history/{uuid}.
Behavior: `limit` defaults to 10 and `offset` defaults to 0 when omitted.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Allowed values:
- nationality: IRANIAN, NON_IRANIAN.
- gender: FEMALE, MALE.
- status: CREATED, CONTACT_INFO_COMPLETED, PROFILE_COMPLETED, SYSTEM_APPROVED, PENDING_ADMIN_APPROVAL, ADMIN_REJECTED, ADMIN_APPROVED.
- kycLevel: LEVEL_1, LEVEL_2, LEVEL_3.
- approval request status: PENDING, APPROVED, REJECTED.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = ProfileHistory::class))
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
    suspend fun getProfileHistory(
        @Parameter(name = "uuid", description = "User UUID depending on the endpoint context.", required = true)
        @PathVariable uuid: String,
        @Parameter(name = "offset", description = "Optional page offset.", required = false)
        @RequestParam offset: Int?,
        @Parameter(name = "limit", description = "Optional page size.", required = false)
        @RequestParam limit: Int?,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): List<ProfileHistory> {
        return profileProxy.getProfileHistory(
            securityContext.jwtAuthentication().tokenValue(),
            uuid,
            limit ?: 10,
            offset ?: 0
        )
    }

    @PostMapping("/approval-requests")
    @Operation(
        summary = "Get approval requests",
        description = """POST /opex/v1/admin/profile/approval-requests.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Allowed values:
- nationality: IRANIAN, NON_IRANIAN.
- gender: FEMALE, MALE.
- status: CREATED, CONTACT_INFO_COMPLETED, PROFILE_COMPLETED, SYSTEM_APPROVED, PENDING_ADMIN_APPROVAL, ADMIN_REJECTED, ADMIN_APPROVED.
- kycLevel: LEVEL_1, LEVEL_2, LEVEL_3.
- approval request status: PENDING, APPROVED, REJECTED.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = ProfileApprovalAdminResponse::class))
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
        @RequestBody request: ProfileApprovalRequestFilter,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): List<ProfileApprovalAdminResponse> {
        return profileProxy.getProfileApprovalRequests(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @GetMapping("/approval-request/{id}")
    @Operation(
        summary = "Get approval request",
        description = """GET /opex/v1/admin/profile/approval-request/{id}.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Allowed values:
- nationality: IRANIAN, NON_IRANIAN.
- gender: FEMALE, MALE.
- status: CREATED, CONTACT_INFO_COMPLETED, PROFILE_COMPLETED, SYSTEM_APPROVED, PENDING_ADMIN_APPROVAL, ADMIN_REJECTED, ADMIN_APPROVED.
- kycLevel: LEVEL_1, LEVEL_2, LEVEL_3.
- approval request status: PENDING, APPROVED, REJECTED.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ProfileApprovalAdminResponse::class)
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
    suspend fun getApprovalRequest(
        @Parameter(name = "id", description = "Numeric resource ID.", required = true)
        @PathVariable("id") id: Long,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): ProfileApprovalAdminResponse {
        return profileProxy.getProfileApprovalRequest(securityContext.jwtAuthentication().tokenValue(), id)
    }

    @PutMapping("/approval-request")
    @Operation(
        summary = "Update approval request status",
        description = """PUT /opex/v1/admin/profile/approval-request.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Allowed values:
- nationality: IRANIAN, NON_IRANIAN.
- gender: FEMALE, MALE.
- status: CREATED, CONTACT_INFO_COMPLETED, PROFILE_COMPLETED, SYSTEM_APPROVED, PENDING_ADMIN_APPROVAL, ADMIN_REJECTED, ADMIN_APPROVED.
- kycLevel: LEVEL_1, LEVEL_2, LEVEL_3.
- approval request status: PENDING, APPROVED, REJECTED.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ProfileApprovalAdminResponse::class)
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
    suspend fun updateApprovalRequestStatus(
        @RequestBody changeRequestStatusBody: UpdateApprovalRequestBody,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): ProfileApprovalAdminResponse {
        return profileProxy.updateProfileApprovalRequest(
            securityContext.jwtAuthentication().tokenValue(),
            changeRequestStatusBody
        )
    }
}
