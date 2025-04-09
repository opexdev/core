package co.nilin.opex.otp.app.controller

import co.nilin.opex.otp.app.data.SetupTOTPRequest
import co.nilin.opex.otp.app.data.VerifyOTPResponse
import co.nilin.opex.otp.app.data.VerifyTOTPRequest
import co.nilin.opex.otp.app.service.TOTPService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/totp")
class TOTPController(private val service: TOTPService) {

    @PostMapping("/setup")
    suspend fun setup(@RequestBody request: SetupTOTPRequest): String {
        return service.setupTOTP(request.userId, request.label)
    }

    @PostMapping("/setup/verify")
    suspend fun verifySetup(@RequestBody request: VerifyTOTPRequest) {
        service.verifyAndMarkActivated(request.userId, request.code)
    }

    @PostMapping("/verify")
    suspend fun verify(@RequestBody request: VerifyTOTPRequest): VerifyOTPResponse {
        val isVerified = service.verifyTOTP(request.userId, request.code)
        return VerifyOTPResponse(isVerified)
    }
}