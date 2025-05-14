package co.nilin.opex.auth.service

import co.nilin.opex.auth.exception.UserNotFoundException
import co.nilin.opex.auth.model.*
import co.nilin.opex.auth.proxy.CaptchaProxy
import co.nilin.opex.auth.proxy.GoogleProxy
import co.nilin.opex.auth.proxy.KeycloakProxy
import co.nilin.opex.auth.proxy.OTPProxy
import co.nilin.opex.common.OpexError
import org.springframework.stereotype.Service

@Service
class TokenService(
    private val otpProxy: OTPProxy,
    private val keycloakProxy: KeycloakProxy,
    private val googleProxy: GoogleProxy,
    private val captchaProxy: CaptchaProxy,
) {

    suspend fun getToken(request: PasswordFlowTokenRequest): TokenResponse {
        captchaProxy.validateCaptcha(request.captchaCode, request.captchaType ?: CaptchaType.INTERNAL)
        val username = Username.create(request.username)
        val user = keycloakProxy.findUserByUsername(username) ?: throw OpexError.UserNotFound.exception()

        val otpType = OTPType.valueOf(user.attributes?.get(Attributes.OTP)?.get(0) ?: OTPType.NONE.name)
        if (otpType == OTPType.NONE) {
            val token = keycloakProxy.getUserToken(
                username,
                request.password,
                request.clientId,
                request.clientSecret
            ).apply { if (!request.rememberMe) refreshToken = null }
            return TokenResponse(token, null)
        }

        if (request.otp.isNullOrBlank()) {
            val requiredOtpTypes = listOf(OTPReceiver(username.value, otpType))
            otpProxy.requestOTP(username.value, requiredOtpTypes)
            val receiver = when (otpType) {
                OTPType.EMAIL -> user.email
                OTPType.SMS -> user.attributes?.get(Attributes.MOBILE)?.get(0)
                else -> null
            }
            return TokenResponse(null, RequiredOTP(otpType, receiver))
        }

        val otpRequest = OTPVerifyRequest(username.value, listOf(OTPCode(request.otp, username.type.otpType)))
        val isOTPValid = otpProxy.verifyOTP(otpRequest)
        if (!isOTPValid) throw OpexError.InvalidOTP.exception()

        val token = keycloakProxy.getUserToken(
            username,
            request.password,
            request.clientId,
            request.clientSecret
        ).apply { if (!request.rememberMe) refreshToken = null }

        return TokenResponse(token, null)
    }

    suspend fun getToken(tokenRequest: ExternalIdpTokenRequest): TokenResponse {
        val idToken = tokenRequest.idToken
        val decodedJWT = googleProxy.validateGoogleToken(idToken)
        val email = decodedJWT.getClaim("email").asString()
            ?: throw IllegalArgumentException("Email not found in Google token")
        try {
            keycloakProxy.findUserByEmail(email)
        } catch (e: Exception) {
            throw UserNotFoundException(email)
        }
        return TokenResponse(
            keycloakProxy.exchangeGoogleTokenForKeycloakToken(
                tokenRequest.accessToken
            ), null
        )
    }

    suspend fun refreshToken(request: RefreshTokenRequest): TokenResponse {
        val token = keycloakProxy.refreshUserToken(request.refreshToken, request.clientId, request.clientSecret)
        return TokenResponse(token, null)
    }
}
