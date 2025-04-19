package co.nilin.opex.auth.controller

import co.nilin.opex.auth.model.ExternalIdpUserRegisterRequest
import co.nilin.opex.auth.model.RegisterUserRequest
import co.nilin.opex.auth.model.TokenResponse
import co.nilin.opex.auth.service.UserService
import co.nilin.opex.common.OpexError
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(private val userService: UserService) {

    @PostMapping("/register")
    suspend fun registerUser(@Valid @RequestBody request: RegisterUserRequest): ResponseEntity<Any> {
        if (request.email.isNullOrBlank() && request.email.isNullOrBlank())
            throw OpexError.InvalidRequestBody.exception()

        userService.registerUser(request)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/register-external")
    suspend fun registerExternal(@RequestBody request: ExternalIdpUserRegisterRequest): ResponseEntity<TokenResponse> {
        userService.registerExternalIdpUser(request);
        return ResponseEntity.ok().build()
    }

}

