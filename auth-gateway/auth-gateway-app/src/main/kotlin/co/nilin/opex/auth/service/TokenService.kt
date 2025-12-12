package co.nilin.opex.auth.service

import co.nilin.opex.auth.data.ActionType
import co.nilin.opex.auth.data.Device
import co.nilin.opex.auth.data.LoginEvent
import co.nilin.opex.auth.kafka.AuthEventProducer
import co.nilin.opex.auth.model.*
import co.nilin.opex.auth.proxy.GoogleProxy
import co.nilin.opex.auth.proxy.KeycloakProxy
import co.nilin.opex.auth.proxy.OTPProxy
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.security.JwtUtils
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TokenService(
    private val otpProxy: OTPProxy,
    private val keycloakProxy: KeycloakProxy,
    private val googleProxy: GoogleProxy,
    private val captchaHandler: CaptchaHandler,
    private val authEventProducer: AuthEventProducer,
) {
    private val logger by LoggerDelegate()
    private val PRE_AUTH_CLIENT_SECRET_KEY = "pY1uVemXFIgVwubP1QM31YxRFh87NRp8"
    private val PRE_AUTH_CLIENT_ID = "pre-auth-client"

    suspend fun requestGetToken(request: PasswordFlowTokenRequest): TokenResponse {
        captchaHandler.validateCaptchaWithActionCache(
            username = request.username,
            captchaCode = request.captchaCode,
            captchaType = request.captchaType,
            action = ActionType.LOGIN
        )
        val username = Username.create(request.username)
        val user = keycloakProxy.findUserByUsername(username) ?: throw OpexError.UserNotFound.exception()

        val otpTypes = (user.attributes?.get(Attributes.OTP)?.get(0) ?: OTPType.NONE.name).split(",")

        if (otpTypes.contains(OTPType.NONE.name)) {
            val token = keycloakProxy.getUserToken(
                username,
                request.password,
                request.clientId,
                request.clientSecret
            ).apply { if (!request.rememberMe) refreshToken = null }
            return TokenResponse(token, null, null)
        }

        keycloakProxy.checkUserCredentials(user, request.password)
        val usernameType = username.type.otpType
        if (!otpTypes.contains((usernameType.name))) throw OpexError.OTPCannotBeRequested.exception()
        val requiredOtpTypes = listOf(OTPReceiver(username.value, usernameType))
        val res = otpProxy.requestOTP(username.value, requiredOtpTypes)
        val receiver = when (usernameType) {
            OTPType.EMAIL -> user.email
            OTPType.SMS -> user.mobile
            else -> null
        }

        val token = keycloakProxy.getUserToken(
            username,
            request.password,
            PRE_AUTH_CLIENT_ID,
            PRE_AUTH_CLIENT_SECRET_KEY,
        ).apply { if (!request.rememberMe) refreshToken = null }

        return TokenResponse(token, RequiredOTP(usernameType, receiver), res.otp)
    }

    suspend fun confirmGetToken(request: ConfirmPasswordFlowTokenRequest): TokenResponse {
        val username = Username.create(request.username)
        val otpRequest = OTPVerifyRequest(username.value, listOf(OTPCode(request.otp, username.type.otpType)))
        val otpResult = otpProxy.verifyOTP(otpRequest)
        if (!otpResult.result) {
            when (otpResult.type) {
                OTPResultType.EXPIRED -> throw OpexError.ExpiredOTP.exception()
                else -> throw OpexError.InvalidOTP.exception()
            }
        }

        val token = keycloakProxy.exchangeUserToken(
            request.token,
            PRE_AUTH_CLIENT_ID,
            PRE_AUTH_CLIENT_SECRET_KEY,
            request.clientId
        ).apply { if (!request.rememberMe) refreshToken = null }
        sendLoginEvent(user.id, token.sessionState, request, token.expiresIn)

        return TokenResponse(token, null, null)
    }

    suspend fun getToken(tokenRequest: ExternalIdpTokenRequest): TokenResponse {
        val idToken = tokenRequest.idToken
        val decodedJWT = googleProxy.validateGoogleToken(idToken)
        val email = decodedJWT.getClaim("email").asString()
            ?: throw IllegalArgumentException("Email not found in Google token")
        try {
            keycloakProxy.findUserByEmail(email)
        } catch (e: Exception) {
            throw OpexError.UserNotFound.exception()
        }
        return TokenResponse(
            keycloakProxy.exchangeGoogleTokenForKeycloakToken(
                tokenRequest.accessToken
            ), null, null
        )
    }

    suspend fun refreshToken(request: RefreshTokenRequest): TokenResponse {
        val token = keycloakProxy.refreshUserToken(request.refreshToken, request.clientId, request.clientSecret)
        sendLoginEvent(extractUserUuidFromToken(token.accessToken), token.sessionState, request,token.expiresIn)
        return TokenResponse(token, null, null)
    }

    private fun sendLoginEvent(userId: String, sessionState: String?, request: Device, expiresIn: Int) {
        authEventProducer.send(
            LoginEvent(
                userId,
                sessionState,
                request.deviceUuid,
                request.appVersion,
                request.osVersion,
                LocalDateTime.now().plusSeconds(expiresIn.toLong()),
                request.os
            )
        )
    }


    private fun extractUserUuidFromToken(token: String): String {
        return JwtUtils.decodePayload(token)["sub"].toString()
    }

}
