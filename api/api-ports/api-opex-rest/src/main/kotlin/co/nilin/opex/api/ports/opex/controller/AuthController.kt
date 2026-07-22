package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.ManualRateLimitGroupType
import co.nilin.opex.api.core.inout.auth.*
import co.nilin.opex.api.core.spi.AuthProxy
import co.nilin.opex.api.core.spi.ManualRateLimiterService
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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange

@RestController
@RequestMapping("/opex/v1/oauth/protocol/openid-connect/")
@Tag(
    name = "Auth Gateway - Token",
    description = "Token, OTP confirmation, external IdP token, and refresh-token operations."
)
class AuthController(private val authProxy: AuthProxy, private val manualRateLimiterService: ManualRateLimiterService) {
    @PostMapping("/token")
    @Operation(
        summary = "Request token",
        description = """POST /opex/v1/oauth/protocol/openid-connect/token.
Security: Public endpoint. No Bearer token is required.

Behavior: Starts password-flow login. If OTP is required, the response contains OTP metadata instead of a final access token.
Allowed values:
- captchaType: INTERNAL, ARCAPTCHA, HCAPTCHA.""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = TokenResponse::class)
                )]
            )
        ]
    )
    suspend fun requestGetToken(@RequestBody tokenRequest: PasswordFlowTokenRequest): ResponseEntity<TokenResponse> {
        val tokenResponse = authProxy.requestGetToken(tokenRequest)
        return ResponseEntity.ok().body(tokenResponse)
    }

    @PostMapping("/token/confirm")
    @Operation(
        summary = "Confirm token request",
        description = """POST /opex/v1/oauth/protocol/openid-connect/token/confirm.
Security: Public endpoint. No Bearer token is required.

Validation: `otp` and the pre-auth `token` returned by the token request flow are required.
Behavior: Completes password-flow login after OTP verification.""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = TokenResponse::class)
                )]
            )
        ]
    )
    suspend fun confirmGetToken(
        @RequestBody tokenRequest: ConfirmPasswordFlowTokenRequest,
        @Parameter(hidden = true) exchange: ServerWebExchange
    ): ResponseEntity<TokenResponse> {
        manualRateLimiterService.check(tokenRequest.username, ManualRateLimitGroupType.VERIFY_OTP, exchange)
        val tokenResponse = authProxy.confirmGetToken(tokenRequest)
        return ResponseEntity.ok().body(tokenResponse)
    }

    @PostMapping("/token/resend-otp")
    @Operation(
        summary = "Resend login OTP",
        description = """POST /opex/v1/oauth/protocol/openid-connect/token/resend-otp.
Security: Bearer pre-auth token required.

Behavior: Resends the OTP for an in-progress login flow.
Source of values: Use the pre-auth token returned by the token request flow.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ResendOtpResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun resendOtp(
        @RequestBody resendOtpRequest: ResendOtpRequest,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
    ): ResponseEntity<ResendOtpResponse> {
        val response = authProxy.resendLoginOtp(resendOtpRequest, securityContext.jwtAuthentication().tokenValue())
        return ResponseEntity.ok().body(response)
    }

    @PostMapping("/token-external")
    @Operation(
        summary = "Request token by external IdP",
        description = """POST /opex/v1/oauth/protocol/openid-connect/token-external.
Security: Public endpoint. No Bearer token is required.

Behavior: Exchanges an external identity-provider token for an Opex token. OTP verification data may be required depending on the account state.""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = TokenResponse::class)
                )]
            )
        ]
    )
    suspend fun getToken(@RequestBody tokenRequest: ExternalIdpTokenRequest): ResponseEntity<TokenResponse> {
        val tokenResponse = authProxy.getToken(tokenRequest)
        return ResponseEntity.ok().body(tokenResponse)
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh token",
        description = """POST /opex/v1/oauth/protocol/openid-connect/refresh.
Security: Public endpoint. No Bearer token is required.

Validation: `refreshToken` and `clientId` are required.
Behavior: Issues a new access token from a valid refresh token.""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = TokenResponse::class)
                )]
            )
        ]
    )
    suspend fun refreshToken(@RequestBody tokenRequest: RefreshTokenRequest): ResponseEntity<TokenResponse> {
        val tokenResponse = authProxy.refreshToken(tokenRequest)
        return ResponseEntity.ok().body(tokenResponse)
    }
}