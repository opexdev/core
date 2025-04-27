package co.nilin.opex.auth.controller

import co.nilin.opex.auth.model.ExternalIdpUserRegisterRequest
import co.nilin.opex.auth.model.RegisterUserRequest
import co.nilin.opex.auth.model.RegisterUserResponse
import co.nilin.opex.auth.model.TokenResponse
import co.nilin.opex.auth.service.UserService
import co.nilin.opex.auth.utils.jwtAuthentication
import co.nilin.opex.auth.utils.tokenValue

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(private val userService: UserService) {

    @PostMapping("/register")
    suspend fun registerUser(@Valid @RequestBody request: RegisterUserRequest): ResponseEntity<RegisterUserResponse> {
        val result = userService.registerUser(request)
        return ResponseEntity.ok().body(result)
    }

    @PostMapping("/register-external")
    suspend fun registerExternal(@RequestBody request: ExternalIdpUserRegisterRequest): ResponseEntity<TokenResponse> {
        userService.registerExternalIdpUser(request)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/logout")
    suspend fun logout(@CurrentSecurityContext securityContext: SecurityContext) {
        userService.logout(securityContext.jwtAuthentication().tokenValue())
    }

}

