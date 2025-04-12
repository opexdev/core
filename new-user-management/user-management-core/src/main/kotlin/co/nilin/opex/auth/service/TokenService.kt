package co.nilin.opex.auth.service;

import co.nilin.opex.auth.config.KeycloakConfig
import co.nilin.opex.auth.exception.UserAlreadyExistsException
import co.nilin.opex.auth.exception.UserNotFoundException
import co.nilin.opex.auth.model.*
import co.nilin.opex.auth.proxy.GoogleProxy
import co.nilin.opex.auth.proxy.KeycloakProxy
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.util.*

@Service
class TokenService(
    @Qualifier("otpWebClient") private val otpClient: WebClient,
    private val keycloakProxy: KeycloakProxy,
    private val googleProxy: GoogleProxy
) {
    suspend fun getToken(tokenRequest: PasswordFlowTokenRequest): TokenResponse {
        val token = keycloakProxy.getToken(tokenRequest.username, tokenRequest.password);
        if (tokenRequest.otpVerifyRequest != null) {
            //TODO verify input otp codes and throw error if not verified
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
