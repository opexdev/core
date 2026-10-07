package co.nilin.opex.auth.controller

import co.nilin.opex.auth.model.*
import co.nilin.opex.auth.service.ChangePasswordService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/user/password")
@Tag(
    name = "User Password",
    description = "Endpoints for changing the password of the authenticated user."
)
class UserPasswordController(private val changePasswordService: ChangePasswordService) {

    @PostMapping("/change")
    @Operation(
        summary = "Request password change",
        description = """POST /v1/user/password/change.
Security: Bearer token is required.

Validation: `currentPassword` and `newPassword` are required.
Behavior: Validates the current password and sends an OTP using the user's active two-factor method (EMAIL if two-factor is not enabled).
Response: A temporary `token` and the OTP `receiver`. The client must send this token with the OTP to the confirm endpoint; the new password is not needed again.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ChangePasswordResponse::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()])
        ]
    )
    suspend fun requestChangePassword(
        @RequestBody request: ChangePasswordRequest,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
    ): ResponseEntity<ChangePasswordResponse> {
        val response = changePasswordService.requestChangePassword(request, securityContext.authentication.name)
        return ResponseEntity.ok().body(response)
    }

    @PostMapping("/change/resend-otp")
    @Operation(
        summary = "Resend password change OTP",
        description = """POST /v1/user/password/change/resend-otp.
Security: Bearer token is required.

Validation: The temporary `token` returned by the change request is required.
Behavior: Resends the OTP for an in-progress password change.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ResendOtpResponse::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()])
        ]
    )
    suspend fun resendChangePasswordOtp(
        @RequestBody request: ResendChangePasswordOtpRequest,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
    ): ResponseEntity<ResendOtpResponse> {
        val response = changePasswordService.resendChangePasswordOtp(request, securityContext.authentication.name)
        return ResponseEntity.ok().body(response)
    }

    @PostMapping("/change/confirm")
    @Operation(
        summary = "Confirm password change",
        description = """POST /v1/user/password/change/confirm.
Security: Bearer token is required.

Validation: `otp` and the temporary `token` returned by the change request are required.
Behavior: Verifies the OTP and changes the user's password in Keycloak.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Password changed successfully."),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()])
        ]
    )
    suspend fun confirmChangePassword(
        @RequestBody request: ConfirmChangePasswordRequest,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
    ) {
        changePasswordService.confirmChangePassword(request, securityContext.authentication.name)
    }
}
