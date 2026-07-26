package co.nilin.opex.auth.service

import co.nilin.opex.auth.kafka.AuthEventProducer
import co.nilin.opex.auth.model.*
import co.nilin.opex.auth.proxy.CaptchaProxy
import co.nilin.opex.auth.proxy.DeviceManagementProxy
import co.nilin.opex.auth.proxy.KeycloakProxy
import co.nilin.opex.auth.proxy.OTPProxy
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.LoggerDelegate
import org.springframework.stereotype.Service

@Service
class ForgetPasswordService(
    private val otpProxy: OTPProxy,
    private val keycloakProxy: KeycloakProxy,
    private val captchaProxy: CaptchaProxy,
    private val authEventProducer: AuthEventProducer,
    private val deviceManagementProxy: DeviceManagementProxy,
    private val tempTokenService: TempTokenService
) {

    private val logger by LoggerDelegate()


    suspend fun forgetPassword(request: ForgotPasswordRequest): TempOtpResponse {
        captchaProxy.validateCaptcha(
            request.captchaCode,
            request.captchaType ?: CaptchaType.INTERNAL
        )
        val uName = Username.create(request.username)
        val otpReceiver = OTPReceiver(uName.value, uName.type.otpType)
        val user = keycloakProxy.findUserByUsername(uName) ?: return TempOtpResponse("", otpReceiver)
        //TODO IMPORTANT: remove in production
        val result = otpProxy.requestOTP(uName.value, listOf(otpReceiver))
        return TempOtpResponse(result.otp, otpReceiver)
    }

    suspend fun verifyForget(request: VerifyOTPRequest): String {
        val username = Username.create(request.username)
        val otpRequest = OTPVerifyRequest(username.value, listOf(OTPCode(request.otp, username.type.otpType)))
        val otpResult = otpProxy.verifyOTP(otpRequest)
        if (!otpResult.result) {
            when (otpResult.type) {
                OTPResultType.EXPIRED -> throw OpexError.ExpiredOTP.exception()
                else -> throw OpexError.InvalidOTP.exception()
            }
        }
        return tempTokenService.generateToken(username.value, OTPAction.FORGET)
    }

    suspend fun confirmForget(request: ConfirmForgetRequest) {
        if (request.newPassword != request.newPasswordConfirmation)
            throw OpexError.InvalidPassword.exception()

        val data = tempTokenService.verifyToken(request.token)
        if (!data.isValid || data.action != OTPAction.FORGET)
            throw OpexError.InvalidRegisterToken.exception()

        val username = Username.create(data.userId)
        val user = keycloakProxy.findUserByUsername(username) ?: return

        keycloakProxy.resetPassword(user.id, request.newPassword)
    }


}
