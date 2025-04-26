package co.nilin.opex.auth.service

import co.nilin.opex.auth.model.*
import co.nilin.opex.auth.proxy.GoogleProxy
import co.nilin.opex.auth.proxy.KeycloakProxy
import co.nilin.opex.auth.proxy.OTPProxy
import co.nilin.opex.auth.utils.UsernameValidator
import co.nilin.opex.common.OpexError
import org.springframework.stereotype.Service

@Service
class UserService(
    private val otpProxy: OTPProxy,
    private val keycloakProxy: KeycloakProxy,
    private val googleProxy: GoogleProxy
) {

    suspend fun registerUser(request: RegisterUserRequest): RegisterUserResponse {
        val usernameType = UsernameValidator.getType(request.username)
        if (usernameType.isUnknown())
            throw OpexError.InvalidUsername.exception()

        checkDuplicateUser(request.username, usernameType)

        val otpType = if (usernameType == UsernameType.EMAIL) "EMAIL" else "SMS"
        if (request.otpCode == null && request.otpTracingCode == null) {
            val response = otpProxy.requestOTP(request.username, listOf(OTPReceiver(request.username, otpType)))
            return RegisterUserResponse(null, response.tracingCode)
        }

        val otpRequest = OTPVerifyRequest(request.otpTracingCode ?: "", listOf(OTPCode(request.otpCode ?: "", otpType)))
        val isOTPValid = otpProxy.verifyOTP(request.username, otpRequest)
        if (!isOTPValid) throw OpexError.InvalidOTP.exception()

        keycloakProxy.createUser(
            request.username,
            request.password,
            usernameType,
            request.firstName,
            request.lastName
        )

        return RegisterUserResponse(request.username, null)
    }

    suspend fun registerExternalIdpUser(externalIdpUserRegisterRequest: ExternalIdpUserRegisterRequest) {
        val decodedJWT = googleProxy.validateGoogleToken(externalIdpUserRegisterRequest.idToken)
        val email = decodedJWT.getClaim("email").asString()
            ?: throw OpexError.GmailNotFoundInToken.exception()
        val googleUserId = decodedJWT.getClaim("sub").asString()
            ?: throw OpexError.UserIDNotFoundInToken.exception()

        val username = email // Use email as the username
        checkDuplicateUser(username, UsernameType.EMAIL)

        if (!UsernameValidator.isValidEmail(username))
            throw OpexError.InvalidUsername.exception()

        val userId = keycloakProxy.createExternalIdpUser(email, username, externalIdpUserRegisterRequest.password)
        keycloakProxy.linkGoogleIdentity(userId, email, googleUserId)
    }

    private suspend fun checkDuplicateUser(username: String, usernameType: UsernameType) {
        val attribute = Attribute(if (usernameType == UsernameType.EMAIL) "email" else "mobile", username)
        val users = keycloakProxy.findUserByAttribute(attribute)
        if (users.isNotEmpty())
            throw OpexError.UserAlreadyExists.exception()
    }

}

