package co.nilin.opex.auth.controller

import co.nilin.opex.auth.model.*
import co.nilin.opex.auth.service.ForgetPasswordService
import co.nilin.opex.auth.service.RegisterService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/user/public")
class PublicUserController(
    private val forgetPasswordService: ForgetPasswordService,
    private val registerService: RegisterService
) {


    //TODO IMPORTANT: remove in production
    @PostMapping("/register")
    suspend fun registerUser(@Valid @RequestBody request: RegisterUserRequest): ResponseEntity<TempOtpResponse> {
        val otpResponse = registerService.registerUser(request)
        return ResponseEntity.ok().body(otpResponse)
    }

    @PostMapping("/register/verify")
    suspend fun verifyRegister(@RequestBody request: VerifyOTPRequest): ResponseEntity<OTPActionTokenResponse> {
        val token = registerService.verifyRegister(request)
        return ResponseEntity.ok(OTPActionTokenResponse(token))
    }

    @PostMapping("/register/confirm")
    suspend fun confirmRegister(@RequestBody request: ConfirmRegisterRequest): ResponseEntity<Token> {
        val loginToken = registerService.confirmRegister(request)
        return ResponseEntity.ok(loginToken)
    }

    @PostMapping("/register-external")
    suspend fun registerExternal(@RequestBody request: ExternalIdpUserRegisterRequest): ResponseEntity<TokenResponse> {
        registerService.registerExternalIdpUser(request)
        return ResponseEntity.ok().build()
    }

    //TODO IMPORTANT: remove in production
    @PostMapping("/forget")
    suspend fun forgetPassword(@RequestBody request: ForgotPasswordRequest): ResponseEntity<TempOtpResponse> {
        val otpResponse = forgetPasswordService.forgetPassword(request)
        return ResponseEntity.ok().body(otpResponse)
    }

    @PostMapping("/forget/verify")
    suspend fun verifyForget(@RequestBody request: VerifyOTPRequest): ResponseEntity<OTPActionTokenResponse> {
        val token = forgetPasswordService.verifyForget(request)
        return ResponseEntity.ok(OTPActionTokenResponse(token))
    }

    @PostMapping("/forget/confirm")
    suspend fun forgetPassword(@RequestBody request: ConfirmForgetRequest): ResponseEntity<Nothing> {
        forgetPasswordService.confirmForget(request)
        return ResponseEntity.ok().build()
    }
}