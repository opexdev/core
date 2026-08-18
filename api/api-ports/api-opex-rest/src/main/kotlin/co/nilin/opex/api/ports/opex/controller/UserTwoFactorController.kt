package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.ConfirmTwoFactorRequest
import co.nilin.opex.api.core.inout.OTPType
import co.nilin.opex.api.core.inout.SetupTOTPResponse
import co.nilin.opex.api.core.inout.TOTPCode
import co.nilin.opex.api.core.inout.TwoFactorRequest
import co.nilin.opex.api.core.inout.TwoFactorResponse
import co.nilin.opex.api.core.inout.auth.*
import co.nilin.opex.api.core.spi.AuthProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
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
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/opex/v1/user/2fa")
@Tag(
    name = "User Two-Factor Configuration",
    description = "Endpoints for managing user two-factor authentication (2FA) settings and TOTP setup."
)
@SecurityRequirement(name = "bearerAuth")
class UserTwoFactorController(private val authProxy: AuthProxy) {

    @GetMapping
    @Operation(
        summary = "Get current two-factor authentication configuration",
        description = """GET /opex/v1/user/2fa.
Security: Bearer token is required.

Behavior: Retrieves the currently active two-factor authentication (2FA) method for the authenticated user.
Possible return values: NONE, EMAIL, SMS, TOTP.""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Two-factor configuration retrieved successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = OTPType::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired.",
                content = [Content()]
            )
        ]
    )
    suspend fun getTwoFactorConfig(
        @Parameter(hidden = true) @CurrentSecurityContext securityContext: SecurityContext
    ): ResponseEntity<OTPType> {
        val response = authProxy.getTwoFactorConfig(securityContext.jwtAuthentication().tokenValue())
        return ResponseEntity.ok(response)
    }

    @PostMapping("/enable/request")
    @Operation(
        summary = "Request enabling two-factor authentication",
        description = """POST /opex/v1/user/2fa/enable/request.
Security: Bearer token is required.

Behavior: Starts the two-factor authentication enable flow for the authenticated user.
Allowed values:
- method: EMAIL, SMS, TOTP""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Two-factor enable request created successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = TwoFactorResponse::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired.",
                content = [Content()]
            )
        ]
    )
    suspend fun requestEnableTwoFactor(
        @RequestBody request: TwoFactorRequest,
        @Parameter(hidden = true) @CurrentSecurityContext securityContext: SecurityContext
    ): ResponseEntity<TwoFactorResponse> {
        val response = authProxy.requestEnableTwoFactor(request, securityContext.jwtAuthentication().tokenValue())
        return ResponseEntity.ok(response)
    }

    @PostMapping("/enable/confirm")
    @Operation(
        summary = "Confirm enabling two-factor authentication",
        description = """POST /opex/v1/user/2fa/enable/confirm.
Security: Bearer token is required.

Behavior: Confirms and activates the two-factor authentication enable flow for the authenticated user.""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Two-factor authentication enabled successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = OTPVerifyResponse::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired.",
                content = [Content()]
            )
        ]
    )
    suspend fun confirmEnableTwoFactor(
        @RequestBody request: ConfirmTwoFactorRequest,
        @Parameter(hidden = true) @CurrentSecurityContext securityContext: SecurityContext
    ): ResponseEntity<OTPVerifyResponse> {
        val response = authProxy.confirmEnableTwoFactor(request, securityContext.jwtAuthentication().tokenValue())
        return ResponseEntity.ok(response)
    }

    @PostMapping("/disable/request")
    @Operation(
        summary = "Request disabling two-factor authentication",
        description = """POST /opex/v1/user/2fa/disable/request.
Security: Bearer token is required.

Behavior: Starts the two-factor authentication disable flow for the authenticated user.""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Two-factor disable request created successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = TwoFactorResponse::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired.",
                content = [Content()]
            )
        ]
    )
    suspend fun requestDisableTwoFactor(
        @RequestBody request: TwoFactorRequest,
        @Parameter(hidden = true) @CurrentSecurityContext securityContext: SecurityContext
    ): ResponseEntity<TwoFactorResponse> {
        val response = authProxy.requestDisableTwoFactor(request, securityContext.jwtAuthentication().tokenValue())
        return ResponseEntity.ok(response)
    }

    @PostMapping("/disable/confirm")
    @Operation(
        summary = "Confirm disabling two-factor authentication",
        description = """POST /opex/v1/user/2fa/disable/confirm.
Security: Bearer token is required.

Behavior: Confirms and disables two-factor authentication for the authenticated user.""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Two-factor authentication disabled successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = OTPVerifyResponse::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired.",
                content = [Content()]
            )
        ]
    )
    suspend fun confirmDisableTwoFactor(
        @RequestBody request: ConfirmTwoFactorRequest,
        @Parameter(hidden = true) @CurrentSecurityContext securityContext: SecurityContext
    ): ResponseEntity<OTPVerifyResponse> {
        val response = authProxy.confirmDisableTwoFactor(request, securityContext.jwtAuthentication().tokenValue())
        return ResponseEntity.ok(response)
    }

    @PostMapping("/totp/setup")
    @Operation(
        summary = "Setup TOTP (Authenticator App)",
        description = """POST /opex/v1/user/2fa/totp/setup.
Security: Bearer token is required.

Behavior: Generates secret key and setup URL (otpauth://...) for setting up Authenticator app (e.g., Google Authenticator).""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "TOTP setup credentials generated successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = SetupTOTPResponse::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired.",
                content = [Content()]
            )
        ]
    )
    suspend fun setupTOTP(
        @Parameter(hidden = true) @CurrentSecurityContext securityContext: SecurityContext
    ): ResponseEntity<SetupTOTPResponse> {
        val response = authProxy.setupTOTP(securityContext.jwtAuthentication().tokenValue())
        return ResponseEntity.ok(response)
    }

    @PostMapping("/totp/verify")
    @Operation(
        summary = "Verify TOTP setup code",
        description = """POST /opex/v1/user/2fa/totp/verify.
Security: Bearer token is required.

Behavior: Verifies the generated TOTP code during the initial authenticator setup phase.""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "TOTP setup code verified successfully."
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired.",
                content = [Content()]
            )
        ]
    )
    suspend fun verifyTOTPSetup(
        @RequestBody request: TOTPCode,
        @Parameter(hidden = true) @CurrentSecurityContext securityContext: SecurityContext
    ): ResponseEntity<Unit> {
        authProxy.verifyTOTPSetup(request, securityContext.jwtAuthentication().tokenValue())
        return ResponseEntity.ok().build()
    }
}