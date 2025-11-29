package co.nilin.opex.auth.service

import co.nilin.opex.auth.data.*
import co.nilin.opex.auth.kafka.AuthEventProducer
import co.nilin.opex.auth.model.*
import co.nilin.opex.auth.proxy.GoogleProxy
import co.nilin.opex.auth.proxy.KeycloakProxy
import co.nilin.opex.auth.proxy.OTPProxy
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.security.JwtUtils
import co.nilin.opex.common.utils.LoggerDelegate
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Service
import java.security.PrivateKey
import java.security.PublicKey
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Service
class UserService(
    private val otpProxy: OTPProxy,
    private val keycloakProxy: KeycloakProxy,
    private val googleProxy: GoogleProxy,
    private val privateKey: PrivateKey,
    private val publicKey: PublicKey,
    private val authProducer: AuthEventProducer,
    private val captchaHandler: CaptchaHandler,
    private val authEventProducer: AuthEventProducer
) {

    private val logger by LoggerDelegate()

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
        return generateToken(username.value, OTPAction.REGISTER)
    }

    suspend fun confirmRegister(request: ConfirmRegisterRequest): Token? {
        val data = verifyToken(request.token)
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

    suspend fun logout(userId: String, sessionId: String) {
        keycloakProxy.logoutSession(userId, sessionId)
        sendLogoutEvent(userId, sessionId)
    }

    suspend fun forgetPassword(request: ForgotPasswordRequest): TempOtpResponse {
        captchaHandler.validateCaptchaWithActionCache(
            username = request.username,
            captchaCode = request.captchaCode,
            captchaType = request.captchaType,
            action = ActionType.FORGET
        )
        val uName = Username.create(request.username)
        val user = keycloakProxy.findUserByUsername(uName) ?: return TempOtpResponse("", null)
        val otpReceiver = OTPReceiver(uName.value, uName.type.otpType)
        //TODO IMPORTANT: remove in production
        val result = otpProxy.requestOTP(uName.value, listOf(otpReceiver))
        return TempOtpResponse(result.otp, otpReceiver)
    }

    suspend fun verifyForget(request: VerifyOTPRequest): String {
        val username = Username.create(request.username)
        val otpRequest = OTPVerifyRequest(username.value, listOf(OTPCode(request.otp, username.type.otpType)))
        val otpResult = otpProxy.verifyOTP(otpRequest)
        if (!otpResult.result) {
            when (otpResult.type) {
                OTPResultType.EXPIRED -> throw OpexError.ExpiredOTP.exception()
                else -> throw OpexError.InvalidOTP.exception()
            }
        }
        return generateToken(username.value, OTPAction.FORGET)
    }

    suspend fun confirmForget(request: ConfirmForgetRequest) {
        if (request.newPassword != request.newPasswordConfirmation)
            throw OpexError.InvalidPassword.exception()

        val data = verifyToken(request.token)
        if (!data.isValid || data.action != OTPAction.FORGET)
            throw OpexError.InvalidRegisterToken.exception()

        val username = Username.create(data.userId)
        val user = keycloakProxy.findUserByUsername(username) ?: return

        keycloakProxy.resetPassword(user.id, request.newPassword)
    }

    suspend fun updateMobile(request: UpdateMobileRequest) {
        keycloakProxy.updateUserMobile(request.userId, request.mobile)
    }

    suspend fun updateEmail(request: UpdateEmailRequest) {
        keycloakProxy.updateUserEmail(request.userId, request.email)
    }

    suspend fun updateName(request: UpdateNameRequest) {
        keycloakProxy.updateUserName(request.userId, request.firstName, request.lastName)
    }

    suspend fun fetchActiveSessions(uuid: String, currentSessionId: String): List<ActiveSession> {
        return keycloakProxy.fetchActiveSessions(uuid, currentSessionId)
    }

    suspend fun logoutSession(uuid: String, sessionId: String) {
        keycloakProxy.logoutSession(uuid, sessionId)
    }

    suspend fun logoutOthers(uuid: String, currentSessionId: String) {
        keycloakProxy.logoutOthers(uuid, currentSessionId)
        sendLogoutEvent(uuid,currentSessionId, true)
    }

    suspend fun logoutAll(uuid: String) {
        keycloakProxy.logoutAll(uuid)
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

    private fun generateToken(userId: String, action: OTPAction): String {
        val issuedAt = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())
        val exp = Date.from(LocalDateTime.now().plusMinutes(2).atZone(ZoneId.systemDefault()).toInstant())
        return Jwts.builder()
            .issuer("opex-auth")
            .claim("userId", userId)
            .claim("action", action)
            .issuedAt(issuedAt)
            .expiration(exp)
            .signWith(privateKey)
            .compact()
    }

    private fun verifyToken(token: String): TokenData {
        try {
            val claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .payload
            return TokenData(true, claims["userId"] as String, OTPAction.valueOf(claims["action"] as String))
        } catch (e: JwtException) {
            logger.error("Could not verify token", e)
            return TokenData(false, "", OTPAction.REGISTER)
        }
    }

    private fun sendLogoutEvent(userId: String, sessionState: String?, others: Boolean?=false) {
        authEventProducer.send(
            LogoutEvent(
                userId,
                sessionState,
                others
            )
        )
    }

    private fun sendLoginEvent(userId: String, sessionState: String?, request: Device, expiresIn: Int) {
        authEventProducer.send(
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
}
