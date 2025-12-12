package co.nilin.opex.auth.controller;

import co.nilin.opex.auth.model.ConfirmPasswordFlowTokenRequest
import co.nilin.opex.auth.model.ExternalIdpTokenRequest
import co.nilin.opex.auth.model.PasswordFlowTokenRequest
import co.nilin.opex.auth.model.RefreshTokenRequest
import co.nilin.opex.auth.model.TokenResponse
import co.nilin.opex.auth.service.TokenService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/oauth/protocol/openid-connect/")
class AuthController(private val tokenService: TokenService) {

    @PostMapping("/token/request")
    suspend fun requestGetToken(@RequestBody tokenRequest: PasswordFlowTokenRequest): ResponseEntity<TokenResponse> {
        val tokenResponse = tokenService.requestGetToken(tokenRequest)
        return ResponseEntity.ok().body(tokenResponse)
    }

    @PostMapping("/token/confirm")
    suspend fun confirmGetToken(@RequestBody tokenRequest: ConfirmPasswordFlowTokenRequest): ResponseEntity<TokenResponse> {
        val tokenResponse = tokenService.confirmGetToken(tokenRequest)
        return ResponseEntity.ok().body(tokenResponse)
    }

    @PostMapping("/token-external")
    suspend fun getToken(@RequestBody tokenRequest: ExternalIdpTokenRequest): ResponseEntity<TokenResponse> {
        val tokenResponse = tokenService.getToken(tokenRequest)
        return ResponseEntity.ok().body(tokenResponse)
    }

    @PostMapping("/refresh")
    suspend fun refreshToken(@RequestBody tokenRequest: RefreshTokenRequest): ResponseEntity<TokenResponse> {
        val tokenResponse = tokenService.refreshToken(tokenRequest)
        return ResponseEntity.ok().body(tokenResponse)
    }
}
