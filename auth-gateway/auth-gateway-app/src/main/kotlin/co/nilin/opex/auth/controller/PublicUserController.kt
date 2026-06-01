package co.nilin.opex.auth.controller

import co.nilin.opex.auth.model.*
import co.nilin.opex.auth.service.ForgetPasswordService
import co.nilin.opex.auth.service.RegisterService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/user/public")
@Tag(
    name = "Auth Gateway - Public User",
    description = "Public registration and password-recovery operations."
)
class PublicUserController(
    private val forgetPasswordService: ForgetPasswordService,
    private val registerService: RegisterService
) {


    @PostMapping("/register")
    @Operation(
        summary = "Register user",
        description = """POST /v1/user/public/register.
Security: Public endpoint. No Bearer token is required.

Behavior: Starts the registration flow and sends OTP if required.
Allowed values:
- captchaType: INTERNAL, ARCAPTCHA, HCAPTCHA.""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(type = "object"))]
            )
        ]
    )
    suspend fun registerUser(@Valid @RequestBody request: RegisterUserRequest): ResponseEntity<TempOtpResponse> {
        val otpResponse = registerService.registerUser(request)
        return ResponseEntity.ok().body(otpResponse)
    }

    @PostMapping("/register/verify")
    @Operation(
        summary = "Verify registration OTP",
        description = """POST /v1/user/public/register/verify.
Security: Public endpoint. No Bearer token is required.

Validation: `username` and `otp` are required.
Behavior: Returns an action token used to confirm registration.""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = OTPActionTokenResponse::class)
                )]
            )
        ]
    )
    suspend fun verifyRegister(@RequestBody request: VerifyOTPRequest): ResponseEntity<OTPActionTokenResponse> {
        val token = registerService.verifyRegister(request)
        return ResponseEntity.ok(OTPActionTokenResponse(token))
    }

    @PostMapping("/register/confirm")
    @Operation(
        summary = "Confirm registration",
        description = """POST /v1/user/public/register/confirm.
Security: Public endpoint. No Bearer token is required.

Validation: `password` and registration action `token` are required.
Behavior: Completes registration and returns login token data.""",
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
    suspend fun confirmRegister(@RequestBody request: ConfirmRegisterRequest): ResponseEntity<Token> {
        val loginToken = registerService.confirmRegister(request)
        return ResponseEntity.ok(loginToken)
    }

    @PostMapping("/register-external")
    @Operation(
        summary = "Register external IdP user",
        description = """POST /v1/user/public/register-external.
Security: Public endpoint. No Bearer token is required.

Behavior: Registers a user from an external identity provider token.
Response body: No response body.""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun registerExternal(@RequestBody request: ExternalIdpUserRegisterRequest): ResponseEntity<TokenResponse> {
        registerService.registerExternalIdpUser(request)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/forget")
    @Operation(
        summary = "Forgot password",
        description = """POST /v1/user/public/forget.
Security: Public endpoint. No Bearer token is required.

Behavior: Starts password-recovery flow and sends OTP if required.
Allowed values:
- captchaType: INTERNAL, ARCAPTCHA, HCAPTCHA.""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(type = "object"))]
            )
        ]
    )
    suspend fun forgetPassword(@RequestBody request: ForgotPasswordRequest): ResponseEntity<TempOtpResponse> {
        val otpResponse = forgetPasswordService.forgetPassword(request)
        return ResponseEntity.ok().body(otpResponse)
    }

    @PostMapping("/forget/verify")
    @Operation(
        summary = "Verify forgot-password OTP",
        description = """POST /v1/user/public/forget/verify.
Security: Public endpoint. No Bearer token is required.

Validation: `username` and `otp` are required.
Behavior: Returns an action token used to confirm password reset.""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = OTPActionTokenResponse::class)
                )]
            )
        ]
    )
    suspend fun verifyForget(@RequestBody request: VerifyOTPRequest): ResponseEntity<OTPActionTokenResponse> {
        val token = forgetPasswordService.verifyForget(request)
        return ResponseEntity.ok(OTPActionTokenResponse(token))
    }

    @PostMapping("/forget/confirm")
    @Operation(
        summary = "Confirm forgot-password flow",
        description = """POST /v1/user/public/forget/confirm.
Security: Public endpoint. No Bearer token is required.

Validation: `newPassword`, `newPasswordConfirmation`, and password-reset action `token` are required.
Response body: No response body.""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun forgetPassword(@RequestBody request: ConfirmForgetRequest): ResponseEntity<Nothing> {
        forgetPasswordService.confirmForget(request)
        return ResponseEntity.ok().build()
    }
}
