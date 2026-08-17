package co.nilin.opex.auth.controller

import co.nilin.opex.auth.model.*
import co.nilin.opex.auth.service.TwoFactorConfigService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/user/2fa")
@Tag(
    name = "User Two-Factor Configuration",
    description = "Endpoints for managing user two-factor authentication (2FA) settings and TOTP setup."
)
class UserTwoFactorConfigController(private val twoFactorConfigService: TwoFactorConfigService) {


    @GetMapping
    @Operation(
        summary = "Get current two-factor authentication configuration",
        description = """
GET /v1/2fa

Security: Bearer token is required.

Behavior:
Retrieves the currently active two-factor authentication (2FA) method for the authenticated user.

Possible return values:
- NONE: Two-factor authentication is disabled.
- EMAIL: 2FA via Email OTP is active.
- SMS: 2FA via SMS OTP is active.
- TOTP: 2FA via Authenticator App (Time-based OTP) is active.
""",
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
            )
        ]
    )
    suspend fun getTwoFactorConfig(@CurrentSecurityContext securityContext: SecurityContext): OTPType {
        return twoFactorConfigService.getTwoFactorConfig(securityContext.authentication.name)
    }

    @PostMapping("/enable/request")
    @Operation(
        summary = "Request enabling two-factor authentication", description = """
POST /v1/2fa/enable/request.

Security: Bearer token is required.

Behavior:
Starts the two-factor authentication enable flow for the authenticated user.

Allowed values:
- method: EMAIL, SMS, TOTP

Response:
- EMAIL/SMS: Returns the OTP receiver information. An OTP is sent to the selected receiver.
- TOTP: Returns the TOTP setup URI (otpauth://...) to be used for QR code generation or manual setup.
""", responses = [ApiResponse(
            responseCode = "200", description = "Two-factor enable request created successfully.", content = [Content(
                mediaType = "application/json", schema = Schema(implementation = TwoFactorResponse::class)
            )]
        )]
    )
    suspend fun requestEnableTwoFactor(
        @RequestBody request: TwoFactorRequest, @CurrentSecurityContext securityContext: SecurityContext
    ): ResponseEntity<TwoFactorResponse> {
        val response = twoFactorConfigService.requestEnableTwoFactor(
            request.method, securityContext.authentication.name
        )
        return ResponseEntity.ok(response)
    }

    @PostMapping("/enable/confirm")
    @Operation(
        summary = "Confirm enabling two-factor authentication", description = """
POST /v1/2fa/enable/confirm.

Security: Bearer token is required.

Behavior:
Confirm the two-factor authentication enable flow for the authenticated user.

Allowed values:
- method: EMAIL, SMS, TOTP
- otp : String

Response:
- Returns the otp result.
""", responses = [ApiResponse(
            responseCode = "200", description = "Two-factor authentication enabled successfully.", content = [Content(
                mediaType = "application/json", schema = Schema(implementation = OTPVerifyResponse::class)
            )]
        )]
    )
    suspend fun confirmEnableTwoFactor(
        @RequestBody request: ConfirmTwoFactorRequest, @CurrentSecurityContext securityContext: SecurityContext
    ): ResponseEntity<OTPVerifyResponse> {
        val response = twoFactorConfigService.confirmEnableTwoFactor(
            request.method,
            request.otp,
            securityContext.authentication.name
        )
        return ResponseEntity.ok(response)
    }

    @PostMapping("/disable/request")
    @Operation(
        summary = "Request disabling two-factor authentication", description = """
POST /v1/2fa/disable/request.

Security: Bearer token is required.

Behavior:
Starts the two-factor authentication disable flow for the authenticated user.

Allowed values:
- method: EMAIL, SMS, TOTP

Response:
- EMAIL/SMS: Returns the OTP receiver information. An OTP is sent to the selected receiver.
- TOTP: Returns the TOTP code.
""", responses = [ApiResponse(
            responseCode = "200", description = "Two-factor enable request created successfully.", content = [Content(
                mediaType = "application/json", schema = Schema(implementation = TwoFactorResponse::class)
            )]
        )]
    )
    suspend fun requestDisableTwoFactor(
        @RequestBody request: TwoFactorRequest, @CurrentSecurityContext securityContext: SecurityContext
    ): ResponseEntity<TwoFactorResponse> {
        val response = twoFactorConfigService.requestDisableTwoFactor(
            request.method, securityContext.authentication.name
        )
        return ResponseEntity.ok(response)
    }

    @PostMapping("/disable/confirm")
    @Operation(
        summary = "Confirm disabling two-factor authentication", description = """
POST /v1/two-factor/disable/confirm.

Security: Bearer token is required.

Behavior:
Confirm the two-factor authentication enable flow for the authenticated user.

Allowed values:
- method: EMAIL, SMS, TOTP
- otp : String

Response:
- Returns the otp result.
""", responses = [ApiResponse(
            responseCode = "200", description = "Two-factor authentication enabled successfully.", content = [Content(
                mediaType = "application/json", schema = Schema(implementation = OTPVerifyResponse::class)
            )]
        )]
    )
    suspend fun confirmDisableTwoFactor(
        @RequestBody request: ConfirmTwoFactorRequest, @CurrentSecurityContext securityContext: SecurityContext
    ): ResponseEntity<OTPVerifyResponse> {
        val response = twoFactorConfigService.confirmDisableTwoFactor(
            request.method,
            request.otp,
            securityContext.authentication.name
        )
        return ResponseEntity.ok(response)

    }

    @PostMapping("/totp/setup")
    @Operation(
        summary = "Setup TOTP (Authenticator App)",
        description = """
POST /v1/user/2fa/totp/setup

Security: Bearer token is required.

Behavior:
Generates secret key and setup URL (otpauth://) for setting up Authenticator app (e.g. Google Authenticator).
""",
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
            )
        ]
    )
    suspend fun setupTOTP(@CurrentSecurityContext securityContext: SecurityContext): SetupTOTPResponse {
        return twoFactorConfigService.setupTOTP(securityContext.authentication.name)
    }

    @PostMapping("/totp/verify")
    @Operation(
        summary = "Verify TOTP setup code",
        description = """
POST /v1/user/2fa/totp/verify

Security: Bearer token is required.

Behavior:
Verifies the generated TOTP code during the initial authenticator setup phase.
""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "TOTP setup code verified successfully."
            )
        ]
    )
    suspend fun verifyTOTPSetup(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: TOTPCode
    ) {
        return twoFactorConfigService.verifyTOTPSetup(securityContext.authentication.name, request.code)
    }
}