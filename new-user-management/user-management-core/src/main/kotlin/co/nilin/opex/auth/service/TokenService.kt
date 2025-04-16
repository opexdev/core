package co.nilin.opex.auth.service;

import co.nilin.opex.auth.exception.UserNotFoundException
import co.nilin.opex.auth.model.ExternalIdpTokenRequest
import co.nilin.opex.auth.model.OTPSendResponse
import co.nilin.opex.auth.model.PasswordFlowTokenRequest
import co.nilin.opex.auth.model.TokenResponse
import co.nilin.opex.auth.proxy.GoogleProxy
import co.nilin.opex.auth.proxy.KeycloakProxy
import co.nilin.opex.auth.proxy.OTPProxy
import org.springframework.stereotype.Service
import java.util.*

@Service
class TokenService(
    private val otpProxy: OTPProxy,
    private val keycloakProxy: KeycloakProxy,
    private val googleProxy: GoogleProxy
) {
    suspend fun getToken(tokenRequest: PasswordFlowTokenRequest): TokenResponse {
        val token = keycloakProxy.getToken(tokenRequest.username, tokenRequest.password)
        if (tokenRequest.otpVerifyRequest != null) {
            val isOTPValid = otpProxy.verifyOTP(tokenRequest.username, tokenRequest.otpVerifyRequest)
            if (!isOTPValid) throw IllegalStateException("Invalid otp verification code")
        } else {
            val requiredOtpTypes = listOf("SMS") //TODO check which type of otp is required for login
            if (!requiredOtpTypes.isEmpty()) {
                //TODO call otp send
                val otpSendResponse = OTPSendResponse(UUID.randomUUID().toString(), requiredOtpTypes)
                return TokenResponse(null, otpSendResponse)
            }
        }
        return TokenResponse(token, null)
    }

    suspend fun getToken(tokenRequest: ExternalIdpTokenRequest): TokenResponse {
        val idToken = tokenRequest.idToken
        val decodedJWT = googleProxy.validateGoogleToken(idToken)
        val email = decodedJWT.getClaim("email").asString()
            ?: throw IllegalArgumentException("Email not found in Google token")
        try {
            keycloakProxy.findUsername(email)
        } catch (e: Exception) {
            throw UserNotFoundException(email)
        }
        return TokenResponse(
            keycloakProxy.exchangeGoogleTokenForKeycloakToken(
                tokenRequest.accessToken
            ), null
        );
    }


}
