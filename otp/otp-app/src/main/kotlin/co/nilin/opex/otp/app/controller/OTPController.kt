package co.nilin.opex.otp.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.otp.app.data.*
import co.nilin.opex.otp.app.model.OTPType
import co.nilin.opex.otp.app.service.OTPService
import org.springframework.web.bind.annotation.PathVariable
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
        validateOTPRequest(request.receivers.map { it.type })
        val tracingCode = if (request.receivers.size == 1)
            otpService.requestOTP(request.receivers[0].receiver, request.receivers[0].type, request.userId)
        else
            otpService.requestCompositeOTP(request.receivers.toSet(), request.userId)
        return NewOTPResponse(tracingCode)
    }

    @PostMapping("/verify")
    suspend fun verifyOTP(@RequestBody request: VerifyOTPRequest): VerifyOTPResponse {
        validateOTPRequest(request.otpCodes.map { it.type })
        val isValid = if (request.otpCodes.size == 1)
            otpService.verifyOTP(request.otpCodes[0].code, request.tracingCode, request.userId)
        else
            otpService.verifyCompositeOTP(request.otpCodes.toSet(), request.tracingCode, request.userId)
        return VerifyOTPResponse(isValid)
    }

    private fun validateOTPRequest(request: List<OTPType>) {
        if (request.isEmpty() || request.contains(OTPType.COMPOSITE))
            throw OpexError.BadRequest.exception()

        val map = request.groupingBy { it }.eachCount()
        map.forEach { entry ->
            if (entry.value > 1)
                throw OpexError.BadRequest.exception()
        }
    }
}