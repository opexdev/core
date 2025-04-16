package co.nilin.opex.auth.service

import co.nilin.opex.auth.exception.UserAlreadyExistsException
import co.nilin.opex.auth.model.ExternalIdpUserRegisterRequest
import co.nilin.opex.auth.model.RegisterUserRequest
import co.nilin.opex.auth.proxy.GoogleProxy
import co.nilin.opex.auth.proxy.KeycloakProxy
import co.nilin.opex.auth.proxy.OTPProxy
import org.springframework.stereotype.Service

@Service
class UserService(
    private val otpProxy: OTPProxy,
    private val keycloakProxy: KeycloakProxy,
    private val googleProxy: GoogleProxy
) {

    suspend fun registerUser(request: RegisterUserRequest) {
        val isOTPValid = otpProxy.verifyOTP(request.username, request.otpVerifyRequest)
        if (!isOTPValid) throw IllegalArgumentException("Invalid OTP")
        keycloakProxy.createUser(request.email, request.username, request.password, request.firstName, request.lastName)
    }

    suspend fun registerExternalIdpUser(externalIdpUserRegisterRequest: ExternalIdpUserRegisterRequest) {
        val decodedJWT = googleProxy.validateGoogleToken(externalIdpUserRegisterRequest.idToken)
        val email = decodedJWT.getClaim("email").asString()
            ?: throw IllegalArgumentException("Email not found in Google token")
        val googleUserId = decodedJWT.getClaim("sub").asString()
            ?: throw IllegalArgumentException("Google user ID (sub) not found in token")
        val username = email // Use email as the username
        try {
            keycloakProxy.findUsername(email)
        } catch (e: Exception) {
            val userId = keycloakProxy.createExternalIdpUser(email, username, externalIdpUserRegisterRequest.password)
            keycloakProxy.linkGoogleIdentity(userId, email, googleUserId)
            return
        }
        throw UserAlreadyExistsException(email)
    }

}

