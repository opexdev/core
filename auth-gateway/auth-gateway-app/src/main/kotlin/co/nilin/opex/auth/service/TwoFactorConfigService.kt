package co.nilin.opex.auth.service

import co.nilin.opex.auth.model.*
import co.nilin.opex.auth.proxy.KeycloakProxy
import co.nilin.opex.auth.proxy.OTPProxy
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.LoggerDelegate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class TwoFactorConfigService(
    private val otpProxy: OTPProxy,
    private val keycloakProxy: KeycloakProxy,
    @Value("\${app.name}")
    private val appName: String,
) {
    private val logger by LoggerDelegate()

    suspend fun getTwoFactorConfig(uuid: String): OTPType =
        getUserByUuid(uuid).currentOtpMethod

    suspend fun requestEnableTwoFactor(method: OTPType, uuid: String): TwoFactorResponse {
        validateMethod(method)
        val user = getUserByUuid(uuid)

        if (user.currentOtpMethod != OTPType.NONE) {
            throw OpexError.InvalidOTPType.exception()
        }

        return when (method) {
            OTPType.EMAIL, OTPType.SMS -> sendOtpRequest(user, method)
            OTPType.TOTP -> {
                val totpConfig = otpProxy.queryTOTP(uuid)
                if (!totpConfig.isActivated || !totpConfig.isEnabled) {
                    throw OpexError.TOTPSetupIncomplete.exception()
                }
                TwoFactorResponse(otp = null, otpReceiver = OTPReceiver("$appName : ${user.username}", OTPType.TOTP))
            }

            OTPType.NONE -> throw OpexError.InvalidOTPType.exception()
        }
    }

    suspend fun confirmEnableTwoFactor(
        method: OTPType,
        otpCode: String,
        uuid: String
    ): OTPVerifyResponse {
        val user = getUserByUuid(uuid)
        val result = verifyTwoFactorCode(user, method, otpCode)
        keycloakProxy.updateOtpConfig(uuid, method.name)
        return result
    }

    suspend fun requestDisableTwoFactor(method: OTPType, uuid: String): TwoFactorResponse {
        validateMethod(method)
        val user = getUserByUuid(uuid)
        if (user.currentOtpMethod == OTPType.NONE || user.currentOtpMethod != method) {
            throw OpexError.InvalidOTPType.exception()
        }
        return when (method) {
            OTPType.EMAIL, OTPType.SMS -> sendOtpRequest(user, method)
            OTPType.TOTP -> TwoFactorResponse(otp = null, otpReceiver = OTPReceiver("$appName : ${user.username}", OTPType.TOTP))
            OTPType.NONE -> throw OpexError.InvalidOTPType.exception()
        }
    }

    suspend fun confirmDisableTwoFactor(
        method: OTPType,
        otpCode: String,
        uuid: String
    ): OTPVerifyResponse {
        val user = getUserByUuid(uuid)
        val result = verifyTwoFactorCode(user, method, otpCode)

        keycloakProxy.updateOtpConfig(uuid, OTPType.NONE.name)
        return result
    }

    suspend fun setupTOTP(uuid: String): SetupTOTPResponse {
        val user = getUserByUuid(uuid)
        val totpResponse = otpProxy.queryTOTP(uuid)
        return when {
            !totpResponse.isEnabled && !totpResponse.isActivated -> otpProxy.setupTOTP(
                uuid,
                "$appName : ${user.username}"
            )

            totpResponse.isEnabled -> SetupTOTPResponse(totpResponse.uri)
            else -> throw OpexError.BadRequest.exception()
        }
    }

    suspend fun verifyTOTPSetup(uuid: String, code: String) {
        val totpConfig = otpProxy.queryTOTP(uuid)
        if (totpConfig.isActivated || !totpConfig.isEnabled) {
            throw OpexError.TOTPAlreadyRegistered.exception()
        }
        otpProxy.verifyTOTPSetup(uuid, code)
        keycloakProxy.updateOtpConfig(uuid, OTPType.TOTP.name)
    }

    // --- Private Helper Methods ---

    private suspend fun verifyTwoFactorCode(
        user: KeycloakUser,
        method: OTPType,
        otpCode: String
    ): OTPVerifyResponse {
        validateMethod(method)

        return when (method) {
            OTPType.EMAIL, OTPType.SMS -> verifyOTP(user, method, otpCode)
            OTPType.TOTP -> {
                val totpResponse = otpProxy.verifyTOTP(userId = user.id, code = otpCode)
                if (!totpResponse.result) throw OpexError.InvalidTOTPCode.exception()
                OTPVerifyResponse(result = true, type = OTPResultType.VALID)
            }

            OTPType.NONE -> throw OpexError.InvalidOTPType.exception()
        }
    }

    private suspend fun getUserByUuid(uuid: String): KeycloakUser =
        keycloakProxy.findUserByUuid(uuid) ?: throw OpexError.NotFound.exception()

    private fun validateMethod(method: OTPType) {
        if (method == OTPType.NONE) throw OpexError.InvalidOTPType.exception()
    }

    private suspend fun sendOtpRequest(user: KeycloakUser, method: OTPType): TwoFactorResponse {
        val destination = user.getDestinationFor(method)
        val receiver = OTPReceiver(destination, method)

        val response = otpProxy.requestOTP(
            destination,
            listOf(receiver),
            OTPAction.TWO_FACTOR
        )

        return TwoFactorResponse(
            otp = response.otp,
            otpReceiver = receiver
        )
    }

    private suspend fun verifyOTP(
        user: KeycloakUser,
        method: OTPType,
        otpCode: String
    ): OTPVerifyResponse {
        val destination = user.getDestinationFor(method)

        val result = otpProxy.verifyOTP(
            OTPVerifyRequest(
                userId = destination,
                otpCodes = listOf(OTPCode(otpCode, method))
            )
        )

        if (!result.result) {
            throw when (result.type) {
                OTPResultType.EXPIRED -> OpexError.ExpiredOTP.exception()
                else -> OpexError.InvalidOTP.exception()
            }
        }

        return result
    }

    // --- Extensions ---

    private val KeycloakUser.currentOtpMethod: OTPType
        get() = attributes?.get(Attributes.OTP)
            ?.firstOrNull()
            ?.let { runCatching { OTPType.valueOf(it) }.getOrNull() }
            ?: OTPType.NONE

    private fun KeycloakUser.getDestinationFor(method: OTPType): String = when (method) {
        OTPType.EMAIL -> email
        OTPType.SMS -> mobile
        else -> null
    } ?: throw OpexError.BadRequest.exception()
}