package co.nilin.opex.auth.service

import co.nilin.opex.auth.data.Device
import co.nilin.opex.auth.data.LoginEvent
import co.nilin.opex.auth.kafka.AuthEventProducer
import co.nilin.opex.auth.model.*
import co.nilin.opex.auth.proxy.CaptchaProxy
import co.nilin.opex.auth.proxy.GoogleProxy
import co.nilin.opex.auth.proxy.KeycloakProxy
import co.nilin.opex.auth.proxy.OTPProxy
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.security.JwtUtils
import co.nilin.opex.common.utils.LoggerDelegate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class LoginService(
    private val otpProxy: OTPProxy,
    private val keycloakProxy: KeycloakProxy,
    private val googleProxy: GoogleProxy,
    private val captchaProxy: CaptchaProxy,
    private val authEventProducer: AuthEventProducer,
    @Value("\${app.pre-auth-client-secret}")
    private val preAuthClientSecretKey: String,
) {
    private val logger by LoggerDelegate()

    private val PRE_AUTH_CLIENT_ID = "pre-auth-client"

    suspend fun requestGetToken(request: PasswordFlowTokenRequest): TokenResponse {
        captchaProxy.validateCaptcha(
            request.captchaCode,
            request.captchaType ?: CaptchaType.INTERNAL
        )

        val username = Username.create(request.username)
        val user = keycloakProxy.findUserByUsername(username)
            ?: throw OpexError.UsernameOrPasswordIsIncorrect.exception()

        val otpType = user.attributes?.get(Attributes.OTP)?.firstOrNull()
            ?.let { runCatching { OTPType.valueOf(it) }.getOrNull() }
            ?: OTPType.NONE

        if (otpType == OTPType.NONE) {
            val token = keycloakProxy.getUserToken(
                username,
                request.password,
                request.clientId,
                request.clientSecret
            ).apply { if (!request.rememberMe) refreshToken = null }

            sendLoginEvent(user.id, token.sessionState, request, token.expiresIn)
            return TokenResponse(token, null, null)
        }

        keycloakProxy.checkUserCredentials(user, request.password)

        val token = keycloakProxy.getUserToken(
            username,
            request.password,
            PRE_AUTH_CLIENT_ID,
            preAuthClientSecretKey
        ).apply {
            refreshToken = null
            refreshExpiresIn = 0
        }

        return when (otpType) {
            OTPType.EMAIL, OTPType.SMS -> {
                val destination = when (otpType) {
                    OTPType.EMAIL -> user.email
                    OTPType.SMS -> user.mobile
                    else -> null
                } ?: throw OpexError.BadRequest.exception()

                val requiredOtpTypes = listOf(OTPReceiver(destination, otpType))
                val res = otpProxy.requestOTP(destination, requiredOtpTypes, OTPAction.LOGIN)

                TokenResponse(
                    token = token,
                    otp = RequiredOTP(otpType, destination),
                    otpCode = res.otp
                )
            }

            OTPType.TOTP -> {
                TokenResponse(
                    token = token,
                    otp = RequiredOTP(OTPType.TOTP, user.id),
                    otpCode = null
                )
            }

            OTPType.NONE -> throw OpexError.InvalidOTPType.exception()
        }
    }

    suspend fun resendLoginOtp(request: ResendOtpRequest, uuid: String): ResendOtpResponse {
        val username = Username.create(request.username)
        val user = keycloakProxy.findUserByUsername(username)
            ?: throw OpexError.UserNotFound.exception()

        if (user.id != uuid) throw OpexError.UnAuthorized.exception()

        return when (val otpType = user.currentOtpMethod) {
            OTPType.EMAIL, OTPType.SMS -> {
                val destination = when (otpType) {
                    OTPType.EMAIL -> user.email
                    OTPType.SMS -> user.mobile
                    else -> null
                } ?: throw OpexError.BadRequest.exception()

                val requiredOtpTypes = listOf(OTPReceiver(destination, otpType))
                val res = otpProxy.requestOTP(destination, requiredOtpTypes, OTPAction.LOGIN)

                ResendOtpResponse(
                    otp = RequiredOTP(otpType, destination),
                    otpCode = res.otp
                )
            }

            OTPType.TOTP -> {
                ResendOtpResponse(
                    otp = RequiredOTP(OTPType.TOTP, user.id),
                    otpCode = null
                )
            }

            OTPType.NONE -> throw OpexError.InvalidOTPType.exception()
        }
    }

    suspend fun confirmGetToken(request: ConfirmPasswordFlowTokenRequest): TokenResponse {
        val username = Username.create(request.username)
        val user = keycloakProxy.findUserByUsername(username)
            ?: throw OpexError.UserNotFound.exception()

        when (val otpType = user.currentOtpMethod) {
            OTPType.EMAIL, OTPType.SMS -> {
                val destination = when (otpType) {
                    OTPType.EMAIL -> user.email
                    OTPType.SMS -> user.mobile
                    else -> null
                } ?: throw OpexError.BadRequest.exception()

                val otpRequest = OTPVerifyRequest(
                    userId = destination,
                    otpCodes = listOf(OTPCode(request.otp, otpType))
                )
                val otpResult = otpProxy.verifyOTP(otpRequest)

                if (!otpResult.result) {
                    throw when (otpResult.type) {
                        OTPResultType.EXPIRED -> OpexError.ExpiredOTP.exception()
                        else -> OpexError.InvalidOTP.exception()
                    }
                }
            }

            OTPType.TOTP -> {
                val totpResult = otpProxy.verifyTOTP(userId = user.id, code = request.otp)
                if (!totpResult.result) {
                    throw OpexError.InvalidTOTPCode.exception()
                }
            }

            OTPType.NONE -> throw OpexError.InvalidOTPType.exception()
        }

        val token = keycloakProxy.getClientBTokenWithBootstrap(
            bootstrapToken = request.token,
            clientId = request.clientId,
            clientSecret = request.clientSecret,
            rememberMe = request.rememberMe
        )

        sendLoginEvent(extractUserUuidFromToken(token.accessToken), token.sessionState, request, token.expiresIn)

        return TokenResponse(token, null, null)
    }

    // --- Helper Extension ---
    private val KeycloakUser.currentOtpMethod: OTPType
        get() = attributes?.get(Attributes.OTP)
            ?.firstOrNull()
            ?.let { runCatching { OTPType.valueOf(it) }.getOrNull() }
            ?: OTPType.NONE

    suspend fun getToken(tokenRequest: ExternalIdpTokenRequest): TokenResponse {
        val idToken = tokenRequest.idToken
        val decodedJWT = googleProxy.validateGoogleToken(idToken)
        val email = decodedJWT.getClaim("email").asString()
            ?: throw IllegalArgumentException("Email not found in Google token")
        try {
            keycloakProxy.findUserByEmail(email)
        } catch (e: Exception) {
            throw OpexError.UsernameOrPasswordIsIncorrect.exception()
        }
        return TokenResponse(
            keycloakProxy.exchangeGoogleTokenForKeycloakToken(
                tokenRequest.accessToken
            ), null, null
        )
    }

    suspend fun refreshToken(request: RefreshTokenRequest): TokenResponse {
        val token = keycloakProxy.refreshUserToken(request.refreshToken, request.clientId, request.clientSecret)
        sendLoginEvent(extractUserUuidFromToken(token.accessToken), token.sessionState, request, token.expiresIn)
        return TokenResponse(token, null, null)
    }

    private fun sendLoginEvent(
        userId: String,
        sessionState: String?,
        request: Device,
        expiresIn: Int
    ) {
        authEventProducer.send(
            LoginEvent(
                uuid = userId,
                deviceUuid = request.deviceUuid,
                appVersion = request.appVersion,
                osVersion = request.osVersion,
                pushToken = request.pushToken,
                os = request.os,
                brand = request.brand,
                model = request.model,
                platform = request.platform,
                agent = request.agent,
                buildNumber = request.buildNumber,
                ipAddress = request.ipAddress,
                sessionId = sessionState ?: "",
                expireDate = LocalDateTime.now().plusSeconds(expiresIn.toLong())
            )
        )
    }


    private fun extractUserUuidFromToken(token: String): String {
        return JwtUtils.decodePayload(token)["sub"].toString()
    }

}
