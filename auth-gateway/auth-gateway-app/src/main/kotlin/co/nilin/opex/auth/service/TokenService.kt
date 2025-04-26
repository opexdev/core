package co.nilin.opex.auth.service

import co.nilin.opex.auth.exception.UserNotFoundException
import co.nilin.opex.auth.model.*
import co.nilin.opex.auth.proxy.GoogleProxy
import co.nilin.opex.auth.proxy.KeycloakProxy
import co.nilin.opex.auth.proxy.OTPProxy
import co.nilin.opex.auth.utils.UsernameValidator
import co.nilin.opex.common.OpexError
import org.springframework.stereotype.Service
import java.util.*

@Service
class TokenService(
    private val otpProxy: OTPProxy,
    private val keycloakProxy: KeycloakProxy,
    private val googleProxy: GoogleProxy
) {

    suspend fun getToken(request: PasswordFlowTokenRequest): TokenResponse {
        val username = Username.create(request.username)
        val user = keycloakProxy.findUserByUsername(username) ?: throw OpexError.UserNotFound.exception()

        val otpType = OTPType.valueOf(user.attributes?.get(Attributes.OTP)?.get(0) ?: "NONE")
        if (request.otpCode == null && request.otpTracingCode == null) {
            val requiredOtpTypes = listOf(OTPReceiver(request.username, otpType))
            val otpSendResponse = otpProxy.requestOTP(request.username, requiredOtpTypes)
            return TokenResponse(null, otpSendResponse.tracingCode)
        }

        val otpRequest = OTPVerifyRequest(request.otpTracingCode ?: "", listOf(OTPCode(request.otpCode ?: "", otpType)))
        val isOTPValid = otpProxy.verifyOTP(request.username, otpRequest)
        if (!isOTPValid) throw OpexError.InvalidOTP.exception()

        val token = keycloakProxy.getUserToken(
            username,
            request.password,
            request.clientId,
            request.clientSecret
        )
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
