package co.nilin.opex.auth.controller

import co.nilin.opex.auth.exception.UserAlreadyExistsException
import co.nilin.opex.auth.model.ErrorResponse
import co.nilin.opex.auth.model.ExternalIdpUserRegisterRequest
import co.nilin.opex.auth.model.RegisterUserRequest
import co.nilin.opex.auth.model.TokenResponse
import co.nilin.opex.auth.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange
import java.time.Instant

@RestController
@RequestMapping("/api/v1/users")
class UserController(private val userService: UserService) {

    @PostMapping("/register")
    suspend fun registerUser(@Valid @RequestBody request: RegisterUserRequest): ResponseEntity<Any> {
        userService.registerUser(request)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/register-external")
    suspend fun registerExternal(@RequestBody request: ExternalIdpUserRegisterRequest) : ResponseEntity<TokenResponse> {
        userService.registerExternalIdpUser(request);
        return ResponseEntity.ok().build()
    }

}

