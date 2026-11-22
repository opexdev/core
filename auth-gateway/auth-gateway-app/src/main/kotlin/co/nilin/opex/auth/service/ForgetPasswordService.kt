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
    private val tempTokenService: TempTokenService,
    private val otpResendService: OtpResendService,
) {

    private val logger by LoggerDelegate()


    suspend fun forgetPassword(request: ForgotPasswordRequest): TempOtpResponse {
        val captchaAcceptedAt = captchaProxy.validateCaptcha(
            request.captchaCode,
            request.captchaType ?: CaptchaType.INTERNAL
        )
        val uName = Username.create(request.username)
        val otpReceiver = OTPReceiver(uName.value, uName.type.otpType)
        val resendToken = otpResendService.issue(OTPAction.FORGET, otpReceiver, captchaAcceptedAt)
        val response = sendRecoveryOtp(uName, otpReceiver)
        return response.copy(resendToken = resendToken)
    }

    suspend fun resendForgetOtp(request: ResendOtpRequest): TempOtpResponse {
        val uName = Username.create(request.username)
        val otpReceiver = OTPReceiver(uName.value, uName.type.otpType)
        return otpResendService.resend(OTPAction.FORGET, otpReceiver, request.resendToken) {
            sendRecoveryOtp(uName, otpReceiver)
        }
    }

    private suspend fun sendRecoveryOtp(username: Username, receiver: OTPReceiver): TempOtpResponse {
        keycloakProxy.findUserByUsername(username) ?: return TempOtpResponse("", receiver)
        val result = otpProxy.requestOTP(username.value, listOf(receiver), OTPAction.FORGET)
        return TempOtpResponse(result.otp, receiver)
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
        if (request.newPassword.isBlank())
            throw OpexError.InvalidPassword.exception()

        val data = tempTokenService.verifyToken(request.token)
        if (!data.isValid || data.action != OTPAction.FORGET)
            throw OpexError.InvalidRegisterToken.exception()

        val username = Username.create(data.userId)
        val user = keycloakProxy.findUserByUsername(username) ?: return

        keycloakProxy.resetPassword(user.id, request.newPassword)
    }


}
