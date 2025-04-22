package co.nilin.opex.auth.service

import co.nilin.opex.auth.exception.UserAlreadyExistsException
import co.nilin.opex.auth.model.*
import co.nilin.opex.auth.proxy.GoogleProxy
import co.nilin.opex.auth.proxy.KeycloakProxy
import co.nilin.opex.auth.proxy.OTPProxy
import co.nilin.opex.common.OpexError
import org.springframework.stereotype.Service

@Service
class UserService(
    private val otpProxy: OTPProxy,
    private val keycloakProxy: KeycloakProxy,
    private val googleProxy: GoogleProxy
) {

    suspend fun registerUser(request: RegisterUserRequest) {
        if (request.email.isNullOrBlank() && request.mobile.isNullOrBlank())
            throw OpexError.BadRequest.exception()
        val username = request.email ?: request.mobile

        val otpRequest = OTPVerifyRequest(request.otpTracingCode, listOf(OTPCode(request.otpCode, request.otpType)))
        val isOTPValid = otpProxy.verifyOTP(username!!, otpRequest)
        if (!isOTPValid) throw OpexError.InvalidOTP.exception()

        val attr = if (request.email.isNullOrBlank())
            Attribute("mobile", request.mobile!!)
        else
            Attribute("email", request.email)

        val users = keycloakProxy.findUserByAttribute(attr)
        if (users.isNotEmpty())
            throw OpexError.UserAlreadyExists.exception()

        keycloakProxy.createUser(
            username!!,
            request.email,
            request.mobile,
            request.password,
            request.firstName,
            request.lastName
        )
    }

    suspend fun registerExternalIdpUser(externalIdpUserRegisterRequest: ExternalIdpUserRegisterRequest) {
        val decodedJWT = googleProxy.validateGoogleToken(externalIdpUserRegisterRequest.idToken)
        val email = decodedJWT.getClaim("email").asString()
            ?: throw IllegalArgumentException("Email not found in Google token")
        val googleUserId = decodedJWT.getClaim("sub").asString()
            ?: throw IllegalArgumentException("Google user ID (sub) not found in token")
        val username = email // Use email as the username
        try {
            keycloakProxy.findUserByEmail(email)
        } catch (e: Exception) {
            val userId = keycloakProxy.createExternalIdpUser(email, username, externalIdpUserRegisterRequest.password)
            keycloakProxy.linkGoogleIdentity(userId, email, googleUserId)
            return
        }
        throw UserAlreadyExistsException(email)
    }

}

