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
        //TODO add validation for mobile and email

        val otpRequest = OTPVerifyRequest(request.otpTracingCode, listOf(OTPCode(request.otpCode, request.otpType)))
        val isOTPValid = otpProxy.verifyOTP(request.username, otpRequest)
        if (!isOTPValid) throw OpexError.InvalidOTP.exception()

        val attribute = Attribute(if (request.loginMethod == LoginMethod.EMAIL) "email" else "mobile", request.username)
        val users = keycloakProxy.findUserByAttribute(attribute)
        if (users.isNotEmpty())
            throw OpexError.UserAlreadyExists.exception()

        keycloakProxy.createUser(
            request.username,
            request.password,
            request.loginMethod,
            request.firstName,
            request.lastName
        )
    }

    suspend fun registerExternalIdpUser(externalIdpUserRegisterRequest: ExternalIdpUserRegisterRequest) {
        val decodedJWT = googleProxy.validateGoogleToken(externalIdpUserRegisterRequest.idToken)
        val email = decodedJWT.getClaim("email").asString()
            ?: throw OpexError.GmailNotFoundInToken.exception()
        val googleUserId = decodedJWT.getClaim("sub").asString()
            ?: throw OpexError.UserIDNotFoundInToken.exception()
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

