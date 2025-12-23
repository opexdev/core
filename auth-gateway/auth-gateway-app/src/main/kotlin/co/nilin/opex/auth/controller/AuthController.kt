package co.nilin.opex.auth.controller;

import co.nilin.opex.auth.model.*
import co.nilin.opex.auth.service.LoginService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/oauth/protocol/openid-connect/")
class AuthController(private val loginService: LoginService) {

    @PostMapping("/token")
    suspend fun requestGetToken(@RequestBody tokenRequest: PasswordFlowTokenRequest): ResponseEntity<TokenResponse> {
        val tokenResponse = loginService.requestGetToken(tokenRequest)
        return ResponseEntity.ok().body(tokenResponse)
    }

    @PostMapping("/token/confirm")
    suspend fun confirmGetToken(@RequestBody tokenRequest: ConfirmPasswordFlowTokenRequest): ResponseEntity<TokenResponse> {
        val tokenResponse = loginService.confirmGetToken(tokenRequest)
        return ResponseEntity.ok().body(tokenResponse)
    }

    @PostMapping("/token/resend-otp")
    suspend fun resendOtp(
        @RequestBody resendOtpRequest: ResendOtpRequest,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): ResponseEntity<ResendOtpResponse> {
        val response = loginService.resendLoginOtp(resendOtpRequest, securityContext.authentication.name)
        return ResponseEntity.ok().body(response)
    }

    @PostMapping("/token-external")
    suspend fun getToken(@RequestBody tokenRequest: ExternalIdpTokenRequest): ResponseEntity<TokenResponse> {
        val tokenResponse = loginService.getToken(tokenRequest)
        return ResponseEntity.ok().body(tokenResponse)
    }

    @PostMapping("/refresh")
    suspend fun refreshToken(@RequestBody tokenRequest: RefreshTokenRequest): ResponseEntity<TokenResponse> {
        val tokenResponse = loginService.refreshToken(tokenRequest)
        return ResponseEntity.ok().body(tokenResponse)
    }
}
