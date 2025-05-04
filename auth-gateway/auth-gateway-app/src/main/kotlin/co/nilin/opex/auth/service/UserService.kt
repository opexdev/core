package co.nilin.opex.auth.service

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

    suspend fun registerUser(request: RegisterUserRequest): RegisterUserResponse {
        val username = Username.create(request.username)
        checkDuplicateUser(username)

        val otpType = username.type.otpType
        if (request.otpCode == null && request.otpTracingCode == null) {
            val response = otpProxy.requestOTP(request.username, listOf(OTPReceiver(request.username, otpType)))
            return RegisterUserResponse(null, response.tracingCode)
        }

        val otpRequest = OTPVerifyRequest(request.otpTracingCode ?: "", listOf(OTPCode(request.otpCode ?: "", otpType)))
        val isOTPValid = otpProxy.verifyOTP(request.username, otpRequest)
        if (!isOTPValid) throw OpexError.InvalidOTP.exception()

        keycloakProxy.createUser(
            username,
            request.password,
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

        val username = Username.create(email) // Use email as the username
        checkDuplicateUser(username)

        val userId = keycloakProxy.createExternalIdpUser(email, username, externalIdpUserRegisterRequest.password)
        keycloakProxy.linkGoogleIdentity(userId, email, googleUserId)
    }

    suspend fun logout(userId: String) {
        keycloakProxy.logout(userId)
    }

    suspend fun forgetPassword(request: ForgetPasswordRequest): String? {
        val username = Username.create(request.username)
        if (request.newPassword != request.newPasswordConfirmation) throw OpexError.InvalidPassword.exception()

        val user = keycloakProxy.findUserByUsername(username) ?: return null
        if (request.otpCode.isNullOrBlank() && request.otpTracingCode.isNullOrBlank()) {
            val otp = otpProxy.requestOTP(request.username, listOf(OTPReceiver(username.value, username.type.otpType)))
            return otp.tracingCode
        } else {
            val req =
                OTPVerifyRequest(request.otpTracingCode!!, listOf(OTPCode(request.otpCode!!, username.type.otpType)))
            val isValid = otpProxy.verifyOTP(request.username, req)
            if (!isValid) throw OpexError.InvalidOTP.exception()
        }

        keycloakProxy.resetPassword(user.id, request.newPassword)
        return null
    }

    private suspend fun checkDuplicateUser(username: Username) {
        keycloakProxy.findUserByUsername(username)?.let { throw OpexError.UserAlreadyExists.exception() }
    }

}

