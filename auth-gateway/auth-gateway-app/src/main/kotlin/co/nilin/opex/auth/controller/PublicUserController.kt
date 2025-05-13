package co.nilin.opex.auth.controller

import co.nilin.opex.auth.model.*
import co.nilin.opex.auth.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/user/public")
class PublicUserController(private val userService: UserService) {

    @PostMapping("/register")
    suspend fun registerUser(@Valid @RequestBody request: RegisterUserRequest): ResponseEntity<Nothing> {
        userService.registerUser(request)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/register/verify")
    suspend fun verifyRegister(@RequestBody request: VerifyOTPRequest): ResponseEntity<OTPActionTokenResponse> {
        val token = userService.verifyRegister(request)
        return ResponseEntity.ok(OTPActionTokenResponse(token))
    }

    @PostMapping("/register/confirm")
    suspend fun confirmRegister(@RequestBody request: ConfirmRegisterRequest): ResponseEntity<Token> {
        val loginToken = userService.confirmRegister(request)
        return ResponseEntity.ok(loginToken)
    }

    @PostMapping("/register-external")
    suspend fun registerExternal(@RequestBody request: ExternalIdpUserRegisterRequest): ResponseEntity<TokenResponse> {
        userService.registerExternalIdpUser(request)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/forget")
    suspend fun forgetPassword(@RequestParam username: String): ResponseEntity<Nothing> {
        userService.forgetPassword(username)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/forget/verify")
    suspend fun verifyForget(@RequestBody request: VerifyOTPRequest): ResponseEntity<OTPActionTokenResponse> {
        val token = userService.verifyForget(request)
        return ResponseEntity.ok(OTPActionTokenResponse(token))
    }

    @PostMapping("/forget/confirm")
    suspend fun forgetPassword(@RequestBody request: ConfirmForgetRequest): ResponseEntity<Nothing> {
        userService.confirmForget(request)
        return ResponseEntity.ok().build()
    }
}