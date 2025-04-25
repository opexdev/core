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
    suspend fun getToken(tokenRequest: PasswordFlowTokenRequest): TokenResponse {
        val usernameType = UsernameValidator.getType(tokenRequest.username)
        if (usernameType.isUnknown()) throw OpexError.InvalidUsername.exception()

        val token = keycloakProxy.getUserToken(tokenRequest.username, tokenRequest.password, usernameType)
        if (tokenRequest.otpVerifyRequest != null) {
            val isOTPValid = otpProxy.verifyOTP(tokenRequest.username, tokenRequest.otpVerifyRequest)
            if (!isOTPValid) throw OpexError.InvalidOTP.exception()
        } else {
            val otpType = if (usernameType == UsernameType.EMAIL) "EMAIL" else "SMS"
            val requiredOtpTypes = listOf(OTPReceiver(tokenRequest.username, otpType))
            val otpSendResponse = otpProxy.requestOTP(tokenRequest.username, requiredOtpTypes)
            return TokenResponse(null, otpSendResponse)
        }
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
}
