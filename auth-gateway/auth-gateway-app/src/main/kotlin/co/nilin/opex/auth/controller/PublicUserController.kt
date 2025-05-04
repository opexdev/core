package co.nilin.opex.auth.controller

import co.nilin.opex.auth.model.*
import co.nilin.opex.auth.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/user/public")
class PublicUserController(private val userService: UserService) {

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


    @PostMapping("/forget")
    suspend fun forgetPassword(@RequestBody request: ForgetPasswordRequest): ResponseEntity<GenericOTPResponse> {
        val result = userService.forgetPassword(request)
        return if (result != null)
            ResponseEntity.ok(GenericOTPResponse(result))
        else
            ResponseEntity.ok().build()
    }
}