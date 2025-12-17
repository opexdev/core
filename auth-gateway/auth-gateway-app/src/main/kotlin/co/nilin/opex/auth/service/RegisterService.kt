package co.nilin.opex.auth.service

import co.nilin.opex.auth.data.ActionType
import co.nilin.opex.auth.data.Device
import co.nilin.opex.auth.data.LoginEvent
import co.nilin.opex.auth.data.UserCreatedEvent
import co.nilin.opex.auth.data.UserRole
import co.nilin.opex.auth.kafka.AuthEventProducer
import co.nilin.opex.auth.model.*
import co.nilin.opex.auth.proxy.GoogleProxy
import co.nilin.opex.auth.proxy.KeycloakProxy
import co.nilin.opex.auth.proxy.OTPProxy
import co.nilin.opex.common.OpexError
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class RegisterService(
    private val otpProxy: OTPProxy,
    private val keycloakProxy: KeycloakProxy,
    private val captchaHandler: CaptchaHandler,
    private val googleProxy: GoogleProxy,
    private val authProducer: AuthEventProducer,
   private val tempTokenService: TempTokenService

    ) {
    //TODO IMPORTANT: remove in production
    suspend fun registerUser(request: RegisterUserRequest): TempOtpResponse {
        captchaHandler.validateCaptchaWithActionCache(
            username = request.username,
            captchaCode = request.captchaCode,
            captchaType = request.captchaType,
            action = ActionType.REGISTER
        )
        val username = Username.create(request.username)
        val userStatus = isUserDuplicate(username)

        val otpType = username.type.otpType
        val otpReceiver = OTPReceiver(request.username, otpType)
        val res = otpProxy.requestOTP(request.username, listOf(otpReceiver))

        if (!userStatus)
            keycloakProxy.createUser(
                username,
                request.firstName,
                request.lastName,
                false
            )
        return TempOtpResponse(res.otp, otpReceiver)
    }

    suspend fun verifyRegister(request: VerifyOTPRequest): String {
        val username = Username.create(request.username)
        val otpRequest = OTPVerifyRequest(username.value, listOf(OTPCode(request.otp, username.type.otpType)))
        val otpResult = otpProxy.verifyOTP(otpRequest)
        if (!otpResult.result) {
            when (otpResult.type) {
                OTPResultType.EXPIRED -> throw OpexError.ExpiredOTP.exception()
                else -> throw OpexError.InvalidOTP.exception()
            }
        }
        return tempTokenService.generateToken(username.value, OTPAction.REGISTER)
    }

    suspend fun confirmRegister(request: ConfirmRegisterRequest): Token? {
        val data = tempTokenService.verifyToken(request.token)
        if (!data.isValid || data.action != OTPAction.REGISTER)
            throw OpexError.InvalidRegisterToken.exception()

        val username = Username.create(data.userId)
        val user = keycloakProxy.findUserByUsername(username)
        if (user == null || user.enabled)
            throw OpexError.BadRequest.exception()

        keycloakProxy.confirmCreateUser(user, request.password)
        keycloakProxy.assignRole(user.id, UserRole.LEVEL_1)

        // Send event to let other services know a user just registered
        val event = UserCreatedEvent(user.id, user.username, user.email, user.mobile, user.firstName, user.lastName)
        authProducer.send(event)

        return if (request.clientId.isNullOrBlank() || request.clientSecret.isNullOrBlank())
            null
        else {
            val token = keycloakProxy.getUserToken(username, request.password, request.clientId, request.clientSecret)
            sendLoginEvent(user.id, token.sessionState, request, token.expiresIn)
            return token
        }
    }

    suspend fun registerExternalIdpUser(externalIdpUserRegisterRequest: ExternalIdpUserRegisterRequest) {
        val decodedJWT = googleProxy.validateGoogleToken(externalIdpUserRegisterRequest.idToken)
        val email = decodedJWT.getClaim("email").asString()
            ?: throw OpexError.GmailNotFoundInToken.exception()
        val googleUserId = decodedJWT.getClaim("sub").asString()
            ?: throw OpexError.UserIDNotFoundInToken.exception()

        val username = Username.create(email) // Use email as the username
        isUserDuplicate(username)

        val userId = keycloakProxy.createExternalIdpUser(email, username, externalIdpUserRegisterRequest.password)
        keycloakProxy.linkGoogleIdentity(userId, email, googleUserId)
    }

    private fun sendLoginEvent(userId: String, sessionState: String?, request: Device, expiresIn: Int) {
        authProducer.send(
            LoginEvent(
                userId,
                sessionState,
                request.deviceUuid,
                request.appVersion,
                request.osVersion,
                LocalDateTime.now().plusSeconds(expiresIn.toLong()),
                request.os
            )
        )
    }

    private suspend fun isUserDuplicate(username: Username): Boolean {
        val user = keycloakProxy.findUserByUsername(username)
        return if (user == null)
            false
        else if (!user.enabled)
            return true
        else
            throw OpexError.UserAlreadyExists.exception()
    }
}