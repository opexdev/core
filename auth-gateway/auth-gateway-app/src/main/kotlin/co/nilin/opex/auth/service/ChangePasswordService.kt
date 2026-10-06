package co.nilin.opex.auth.service

import co.nilin.opex.auth.model.*
import co.nilin.opex.auth.proxy.KeycloakProxy
import co.nilin.opex.auth.proxy.OTPProxy
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.LoggerDelegate
import org.springframework.stereotype.Service

@Service
class ChangePasswordService(
    private val otpProxy: OTPProxy,
    private val keycloakProxy: KeycloakProxy,
    private val tempTokenService: TempTokenService
) {
    private val logger by LoggerDelegate()

    suspend fun requestChangePassword(request: ChangePasswordRequest, uuid: String): ChangePasswordResponse {
        if (request.newPassword.isBlank() || request.newPassword != request.newPasswordConfirmation)
            throw OpexError.InvalidPassword.exception()

        val user = getUserByUuid(uuid)
        keycloakProxy.checkUserCredentials(user, request.currentPassword)

        val otpType = user.changePasswordOtpMethod
        val token = tempTokenService.generateChangePasswordToken(user.id, otpType, request.newPassword)
        val (otp, otpCode) = sendOtp(user, otpType)

        return ChangePasswordResponse(token, otp, otpCode)
    }

    suspend fun resendChangePasswordOtp(request: ResendChangePasswordOtpRequest, uuid: String): ResendOtpResponse {
        val data = verifyToken(request.token, uuid)
        val user = getUserByUuid(uuid)
        val (otp, otpCode) = sendOtp(user, data.otpType)
        return ResendOtpResponse(otp, otpCode)
    }

    suspend fun confirmChangePassword(request: ConfirmChangePasswordRequest, uuid: String) {
        val data = verifyToken(request.token, uuid)
        val user = getUserByUuid(uuid)

        when (data.otpType) {
            OTPType.EMAIL, OTPType.SMS -> {
                val otpResult = otpProxy.verifyOTP(
                    OTPVerifyRequest(
                        userId = user.getDestinationFor(data.otpType),
                        otpCodes = listOf(OTPCode(request.otp, data.otpType))
                    )
                )
                if (!otpResult.result) {
                    throw when (otpResult.type) {
                        OTPResultType.EXPIRED -> OpexError.ExpiredOTP.exception()
                        else -> OpexError.InvalidOTP.exception()
                    }
                }
            }

            OTPType.TOTP -> {
                val totpResult = otpProxy.verifyTOTP(userId = user.id, code = request.otp)
                if (!totpResult.result) throw OpexError.InvalidTOTPCode.exception()
            }

            OTPType.NONE -> throw OpexError.InvalidOTPType.exception()
        }

        keycloakProxy.resetPassword(user.id, data.newPassword)
    }

    private fun verifyToken(token: String, uuid: String): ChangePasswordTokenData {
        val data = tempTokenService.verifyChangePasswordToken(token)
        if (!data.isValid) throw OpexError.InvalidToken.exception()
        if (data.userId != uuid) throw OpexError.UnAuthorized.exception()
        return data
    }

    private suspend fun sendOtp(user: KeycloakUser, otpType: OTPType): Pair<RequiredOTP, String?> {
        return when (otpType) {
            OTPType.EMAIL, OTPType.SMS -> {
                val destination = user.getDestinationFor(otpType)
                val res = otpProxy.requestOTP(
                    destination,
                    listOf(OTPReceiver(destination, otpType)),
                    OTPAction.CHANGE_PASSWORD
                )
                RequiredOTP(otpType, destination) to res.otp
            }

            OTPType.TOTP -> RequiredOTP(OTPType.TOTP, user.id) to null
            OTPType.NONE -> throw OpexError.InvalidOTPType.exception()
        }
    }

    private suspend fun getUserByUuid(uuid: String): KeycloakUser =
        keycloakProxy.findUserByUuid(uuid) ?: throw OpexError.UserNotFound.exception()

    // Uses the user's active 2FA method; falls back to EMAIL when 2FA is not enabled
    // (or to SMS when the user has no email)
    private val KeycloakUser.changePasswordOtpMethod: OTPType
        get() {
            val current = attributes?.get(Attributes.OTP)
                ?.firstOrNull()
                ?.let { runCatching { OTPType.valueOf(it) }.getOrNull() }
                ?: OTPType.NONE
            return when {
                current != OTPType.NONE -> current
                !email.isNullOrBlank() -> OTPType.EMAIL
                !mobile.isNullOrBlank() -> OTPType.SMS
                else -> throw OpexError.BadRequest.exception()
            }
        }

    private fun KeycloakUser.getDestinationFor(method: OTPType): String = when (method) {
        OTPType.EMAIL -> email
        OTPType.SMS -> mobile
        else -> null
    } ?: throw OpexError.BadRequest.exception()
}
