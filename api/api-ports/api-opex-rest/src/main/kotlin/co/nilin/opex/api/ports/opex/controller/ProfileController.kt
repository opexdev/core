package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.ProfileProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.toProfileApprovalRequestUserResponse
import co.nilin.opex.api.ports.opex.util.toProfileResponse
import co.nilin.opex.api.ports.opex.util.tokenValue
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/opex/v1/profile")

@Tag(
    name = "Profile",
    description = "Authenticated user profile operations."
)
class ProfileController(
    val profileProxy: ProfileProxy
) {

    @GetMapping("/personal-data")
    @Operation(
        summary = "Get profile",
        description = """GET /opex/v1/profile/personal-data.
Security: Bearer user-token required. Requires authenticated user JWT.
Allowed values:
- nationality: IRANIAN, NON_IRANIAN.
- gender: FEMALE, MALE.
- approval request status: PENDING, APPROVED, REJECTED.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ProfileResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getProfile(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): ProfileResponse {
        return profileProxy.getProfile(securityContext.jwtAuthentication().tokenValue()).toProfileResponse()
    }

    @PutMapping("/completion")
    @Operation(
        summary = "Complete profile",
        description = """PUT /opex/v1/profile/completion.
Security: Bearer user-token required. Requires authenticated user JWT.
Allowed values:
- nationality: IRANIAN, NON_IRANIAN.
- gender: FEMALE, MALE.
- approval request status: PENDING, APPROVED, REJECTED.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ProfileResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun completeProfile(
        @RequestBody completeProfileRequest: CompleteProfileRequest,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): ProfileResponse? {
        return profileProxy.completeProfile(securityContext.jwtAuthentication().tokenValue(), completeProfileRequest)
            ?.toProfileResponse()
    }

    @PostMapping("/contact/update/otp-request")
    @Operation(
        summary = "Request contact update",
        description = """POST /opex/v1/profile/contact/update/otp-request.
Behavior: Starts contact update flow and returns OTP delivery information.
Security: Bearer user-token required. Requires authenticated user JWT.
""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = TempOtpResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun requestContactUpdate(
        @RequestBody request: ContactUpdateRequest,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): TempOtpResponse {
        return profileProxy.requestContactUpdate(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @PostMapping("/contact/update/otp-verification")
    @Operation(
        summary = "Confirm contact update",
        description = """PATCH /opex/v1/profile/contact/update/otp-verification.
Validation: Verification request must contain the OTP value and target contact details required by the request schema.
Security: Bearer user-token required. Requires authenticated user JWT.
""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "No response body.", content = [Content()]),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun confirmContactUpdate(
        @RequestBody request: ContactUpdateConfirmRequest,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        profileProxy.confirmContactUpdate(securityContext.jwtAuthentication().tokenValue(), request)

    }

    @GetMapping("/approval-request")
    @Operation(
        summary = "Get approval request",
        description = """GET /opex/v1/profile/approval-request.
Security: Bearer user-token required. Requires authenticated user JWT.
Allowed values:
- nationality: IRANIAN, NON_IRANIAN.
- gender: FEMALE, MALE.
- approval request status: PENDING, APPROVED, REJECTED.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ProfileApprovalRequestUserResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getApprovalRequest(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): ProfileApprovalRequestUserResponse {
        return profileProxy.getUserProfileApprovalRequest(securityContext.jwtAuthentication().tokenValue())
            .toProfileApprovalRequestUserResponse()
    }
}
