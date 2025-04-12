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
    suspend fun requestOTP(@RequestBody request: List<OTPReceiver>): NewOTPResponse {
        validateOTPRequest(request.map { it.type })
        val tracingCode = if (request.size == 1)
            otpService.requestOTP(request[0].receiver, request[0].type)
        else
            otpService.requestCompositeOTP(request.toSet())
        return NewOTPResponse(tracingCode)
    }

    @PostMapping("/verify/{tracingCode}")
    suspend fun verifyOTP(@PathVariable tracingCode: String, @RequestBody request: List<OTPCode>): VerifyOTPResponse {
        validateOTPRequest(request.map { it.type })
        val isValid = if (request.size == 1)
            otpService.verifyOTP(request[0].code, tracingCode)
        else
            otpService.verifyCompositeOTP(request.toSet(), tracingCode)
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