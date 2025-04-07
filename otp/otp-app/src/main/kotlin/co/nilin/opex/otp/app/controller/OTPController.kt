package co.nilin.opex.otp.app.controller

import co.nilin.opex.otp.app.data.NewOTPRequest
import co.nilin.opex.otp.app.data.NewOTPResponse
import co.nilin.opex.otp.app.data.VerifyOTPRequest
import co.nilin.opex.otp.app.data.VerifyOTPResponse
import co.nilin.opex.otp.app.service.OTPService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/v1/otp")
class OTPController(private val otpService: OTPService) {

    @PostMapping
    suspend fun requestOTP(@RequestBody request: NewOTPRequest): NewOTPResponse {
        val tracingCode = otpService.requestOTP(request.receiver, request.type)
        return NewOTPResponse(tracingCode)
    }

    @PostMapping("/verify")
    suspend fun verifyOTP(@RequestBody request: VerifyOTPRequest): VerifyOTPResponse {
        val isValid = otpService.verifyOTP(request.code, request.tracingCode)
        return VerifyOTPResponse(isValid)
    }
}