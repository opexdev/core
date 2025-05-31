package co.nilin.opex.otp.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.otp.app.data.*
import co.nilin.opex.otp.app.model.OTPType
import co.nilin.opex.otp.app.service.OTPService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/otp")
class OTPController(private val otpService: OTPService) {

    //TODO IMPORTANT: remove in production
    data class TempOtpResponse(val otp: String)
    //TODO IMPORTANT: remove in production

    //TODO IMPORTANT: remove in production
    @PostMapping
    suspend fun requestOTP(@RequestBody request: NewOTPRequest): ResponseEntity<TempOtpResponse> {
        validateOTPRequest(request.receivers.map { it.type })
        val code = if (request.receivers.size == 1)
            otpService.requestOTP(
                request.receivers[0].receiver,
                request.receivers[0].type,
                request.userId,
                request.action
            )
        else
            otpService.requestCompositeOTP(request.receivers.toSet(), request.userId, request.action)
        return ResponseEntity.status(HttpStatus.CREATED).body(TempOtpResponse(code))
    }

    @PostMapping("/verify")
    suspend fun verifyOTP(@RequestBody request: VerifyOTPRequest): OTPResult {
        validateOTPRequest(request.otpCodes.map { it.type })
        val result = if (request.otpCodes.size == 1)
            otpService.verifyOTP(request.otpCodes[0].code, request.userId)
        else
            otpService.verifyCompositeOTP(request.otpCodes.toSet(), request.userId)
        return OTPResult(result.isValid, result)
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