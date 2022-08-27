package co.nilin.opex.auth.gateway.extension

import co.nilin.opex.auth.gateway.ApplicationContextHolder
import co.nilin.opex.auth.gateway.data.*
import co.nilin.opex.auth.gateway.model.ActionTokenResult
import co.nilin.opex.auth.gateway.model.AuthEvent
import co.nilin.opex.auth.gateway.model.UserCreatedEvent
import co.nilin.opex.auth.gateway.utils.*
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.HttpClientBuilder
import org.keycloak.authentication.actiontoken.execactions.ExecuteActionsActionToken
import org.keycloak.common.util.Time
import org.keycloak.email.EmailTemplateProvider
import org.keycloak.models.Constants
import org.keycloak.models.KeycloakSession
import org.keycloak.models.UserCredentialModel
import org.keycloak.models.UserModel
import org.keycloak.models.credential.OTPCredentialModel
import org.keycloak.models.utils.CredentialValidation
import org.keycloak.models.utils.HmacOTP
import org.keycloak.policy.PasswordPolicyManagerProvider
import org.keycloak.services.managers.AuthenticationManager
import org.keycloak.services.resource.RealmResourceProvider
import org.keycloak.urls.UrlType
import org.keycloak.utils.CredentialHelper
import org.keycloak.utils.TotpUtils
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import java.util.concurrent.TimeUnit
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriBuilder
import kotlin.streams.toList

class UserManagementResource(private val session: KeycloakSession) : RealmResourceProvider {

    private val logger = LoggerFactory.getLogger(UserManagementResource::class.java)
    private val opexRealm = session.realms().getRealm("opex")
    private val verifyUrl by lazy {
        ApplicationContextHolder.getCurrentContext()!!.environment.resolvePlaceholders("\${verify-redirect-url}")
    }
    private val forgotUrl by lazy {
        ApplicationContextHolder.getCurrentContext()!!.environment.resolvePlaceholders("\${forgot-redirect-url}")
    }
    private val kafkaTemplate by lazy {
        ApplicationContextHolder.getCurrentContext()!!.getBean("authKafkaTemplate") as KafkaTemplate<String, AuthEvent>
    }

    @POST
    @Path("user")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun registerUser(request: RegisterUserRequest): Response {
        val auth = ResourceAuthenticator.bearerAuth(session)
        if (!auth.hasScopeAccess("trust")) return ErrorHandler.forbidden()

        runCatching {
            validateCaptcha("${request.captchaAnswer}-${session.context.connection.remoteAddr}")
        }.onFailure {
            return ErrorHandler.response(Response.Status.BAD_REQUEST, OpexError.InvalidCaptcha)
        }

        if (!request.isValid())
            return ErrorHandler.response(Response.Status.BAD_REQUEST, OpexError.BadRequest)

        if (session.users().getUserByEmail(request.email, opexRealm) != null)
            return ErrorHandler.response(Response.Status.BAD_REQUEST, OpexError.UserAlreadyExists)

        if (request.password != request.passwordConfirmation)
            return ErrorHandler.badRequest("Invalid password confirmation")

        val error = session.getProvider(PasswordPolicyManagerProvider::class.java)
            .validate(request.email, request.password)

        if (error != null) {
            logger.error(error.message)
            return ErrorHandler.response(Response.Status.BAD_REQUEST, OpexError.InvalidPassword)
        }

        val user = session.users().addUser(opexRealm, request.email).apply {
            email = request.email
            firstName = request.firstName
            lastName = request.lastName
            isEnabled = true
            isEmailVerified = false

            addRequiredAction(UserModel.RequiredAction.VERIFY_EMAIL)
            val actions = requiredActionsStream.toList()
            val token = ActionTokenHelper.generateRequiredActionsToken(session, opexRealm, this, actions)
            val url = "${session.context.getUri(UrlType.BACKEND).baseUri}/realms/opex/user-management/user/verify"
            val link = ActionTokenHelper.attachTokenToLink(url, token)
            val expiration = TimeUnit.SECONDS.toMinutes(opexRealm.actionTokenGeneratedByAdminLifespan.toLong())
            logger.info(link)
            sendEmail(this) { it.sendVerifyEmail(link, expiration) }
        }

        session.userCredentialManager()
            .updateCredential(opexRealm, user, UserCredentialModel.password(request.password, false))

        logger.info("User create response ${user.id}")
        sendUserEvent(user)

        return Response.ok(RegisterUserResponse(user.id)).build()
    }

    @POST
    @Path("user/request-forgot")
    @Produces(MediaType.APPLICATION_JSON)
    fun forgotPassword(
        @QueryParam("email") email: String?,
        @QueryParam("captcha") captcha: String
    ): Response {
        val uri = UriBuilder.fromUri(forgotUrl)

        runCatching {
            validateCaptcha("$captcha-${session.context.connection.remoteAddr}")
        }.onFailure {
            return ErrorHandler.response(Response.Status.BAD_REQUEST, OpexError.InvalidCaptcha)
        }

        val user = session.users().getUserByEmail(email, opexRealm)
        if (user != null) {
            val token = ActionTokenHelper.generateRequiredActionsToken(
                session,
                opexRealm,
                user,
                listOf(UserModel.RequiredAction.UPDATE_PASSWORD.name),
                verifyUrl
            )

            val link = uri.queryParam("key", token).build().toString()
            val expiration = TimeUnit.SECONDS.toMinutes(opexRealm.actionTokenGeneratedByAdminLifespan.toLong())
            logger.info(link)
            logger.info(expiration.toString())
            sendEmail(user) { it.sendVerifyEmail(link, expiration) }
        }

        return Response.noContent().build()
    }

    @PUT
    @Path("user/forgot")
    fun forgotPassword(@QueryParam("key") key: String, body: ForgotPasswordRequest): Response {
        val actionToken = session.tokens().decode(key, ExecuteActionsActionToken::class.java)

        if (actionToken == null || !actionToken.isActive || actionToken.requiredActions.isEmpty())
            return ErrorHandler.badRequest()

        val user = session.users().getUserById(actionToken.subject, opexRealm) ?: return ErrorHandler.userNotFound()
        if (body.password != body.passwordConfirmation)
            return ErrorHandler.badRequest("Invalid password confirmation")

        val error = session.getProvider(PasswordPolicyManagerProvider::class.java)
            .validate(user.email, body.password)

        if (error != null) {
            logger.error(error.message)
            return ErrorHandler.response(Response.Status.BAD_REQUEST, OpexError.InvalidPassword)
        }
        session.userCredentialManager()
            .updateCredential(opexRealm, user, UserCredentialModel.password(body.password, false))

        return Response.noContent().build()
    }

    @GET
    @Path("user/verify")
    fun verifyEmail(@QueryParam("key") key: String): Response {
        val uri = UriBuilder.fromUri(verifyUrl)
        val actionToken = session.tokens().decode(key, ExecuteActionsActionToken::class.java)

        if (actionToken == null || !actionToken.isActive || actionToken.requiredActions.isEmpty())
            return Response.seeOther(uri.queryParam("result", ActionTokenResult.FAILED).build()).build()

        val user = session.users().getUserById(actionToken.subject, opexRealm)
        if (actionToken.requiredActions.contains(UserModel.RequiredAction.VERIFY_EMAIL.name)) {
            user.removeRequiredAction(UserModel.RequiredAction.VERIFY_EMAIL)
            user.isEmailVerified = true
        }

        return Response.seeOther(uri.queryParam("result", ActionTokenResult.SUCCEED).build()).build()
    }

    @POST
    @Path("user/request-verify")
    @Produces(MediaType.APPLICATION_JSON)
    fun sendVerifyEmail(@QueryParam("email") email: String?): Response {
        val user = session.users().getUserByEmail(email, opexRealm)
        if (user != null) {
            val token = ActionTokenHelper.generateRequiredActionsToken(
                session,
                opexRealm,
                user,
                listOf(UserModel.RequiredAction.VERIFY_EMAIL.name)
            )

            val url = "${session.context.getUri(UrlType.BACKEND).baseUri}/realms/opex/user-management/user/verify"
            val link = ActionTokenHelper.attachTokenToLink(url, token)
            val expiration = TimeUnit.SECONDS.toMinutes(opexRealm.actionTokenGeneratedByAdminLifespan.toLong())
            logger.info(link)
            sendEmail(user) { it.sendVerifyEmail(link, expiration) }
        }

        return Response.noContent().build()
    }

    @PUT
    @Path("user/security/password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun changePassword(body: ChangePasswordRequest): Response {
        // AccountFormService
        val auth = ResourceAuthenticator.bearerAuth(session)
        if (!auth.hasScopeAccess("trust")) return ErrorHandler.forbidden()

        val user = session.users().getUserById(auth.getUserId(), opexRealm) ?: return ErrorHandler.userNotFound()

        val cred = UserCredentialModel.password(body.password)
        if (!session.userCredentialManager().isValid(opexRealm, user, cred))
            return ErrorHandler.forbidden("Incorrect password")

        if (body.confirmation != body.newPassword)
            return ErrorHandler.badRequest("Invalid password confirmation")

        session.userCredentialManager()
            .updateCredential(opexRealm, user, UserCredentialModel.password(body.newPassword, false))

        return Response.noContent().build()
    }

    @GET
    @Path("user/security/otp")
    @Produces(MediaType.APPLICATION_JSON)
    fun get2FASecret(): Response {
        val auth = ResourceAuthenticator.bearerAuth(session)
        if (!auth.hasScopeAccess("trust")) return ErrorHandler.forbidden()

        val user = session.users().getUserById(auth.getUserId(), opexRealm) ?: return ErrorHandler.userNotFound()
        if (is2FAEnabled(user)) return ErrorHandler.response(Response.Status.BAD_REQUEST, OpexError.OTPAlreadyEnabled)

        val secret = HmacOTP.generateSecret(64)
        val uri = OTPUtils.generateOTPKeyURI(opexRealm, secret, "Opex", user.email)
        val qr = TotpUtils.qrCode(secret, opexRealm, user)
        return Response.ok(Get2FAResponse(uri, secret, qr)).build()
    }

    @POST
    @Path("user/security/otp")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun setup2FA(body: Setup2FARequest): Response {
        val auth = ResourceAuthenticator.bearerAuth(session)
        if (!auth.hasScopeAccess("trust")) return ErrorHandler.forbidden()

        val user = session.users().getUserById(auth.getUserId(), opexRealm) ?: return ErrorHandler.userNotFound()
        if (is2FAEnabled(user)) return ErrorHandler.response(Response.Status.BAD_REQUEST, OpexError.OTPAlreadyEnabled)

        val otpCredential = OTPCredentialModel.createFromPolicy(opexRealm, body.secret)
        if (!CredentialValidation.validOTP(
                body.initialCode, otpCredential, opexRealm.otpPolicy.lookAheadWindow
            )
        ) return ErrorHandler.response(Response.Status.BAD_REQUEST, OpexError.InvalidOTP)

        CredentialHelper.createOTPCredential(session, opexRealm, user, body.initialCode, otpCredential)
        return Response.noContent().build()
    }

    @DELETE
    @Path("user/security/otp")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun disable2FA(): Response {
        val auth = ResourceAuthenticator.bearerAuth(session)
        if (!auth.hasScopeAccess("trust")) return ErrorHandler.forbidden()

        val user = session.users().getUserById(auth.getUserId(), opexRealm) ?: return ErrorHandler.userNotFound()

        val response = Response.noContent().build()
        if (!is2FAEnabled(user)) return response

        session.userCredentialManager().getStoredCredentialsByTypeStream(opexRealm, user, OTPCredentialModel.TYPE)
            .toList().find { it.type == OTPCredentialModel.TYPE }
            ?.let { session.userCredentialManager().removeStoredCredential(opexRealm, user, it.id) }

        return response
    }

    @GET
    @Path("user/security/check")
    @Produces(MediaType.APPLICATION_JSON)
    fun is2FAEnabled(@QueryParam("username") username: String?): Response {
        val auth = ResourceAuthenticator.bearerAuth(session)
        if (!auth.hasScopeAccess("trust")) return ErrorHandler.forbidden()

        val user = session.users().getUserByUsername(username, opexRealm) ?: return Response.ok(
            UserSecurityCheckResponse(false)
        ).build()

        return Response.ok(UserSecurityCheckResponse(is2FAEnabled(user))).build()
    }

    @POST
    @Path("user/logout")
    @Produces(MediaType.APPLICATION_JSON)
    fun logout(): Response {
        val auth = ResourceAuthenticator.bearerAuth(session)
        if (!auth.hasScopeAccess("trust")) return ErrorHandler.forbidden()

        val userSession = session.sessions().getUserSession(opexRealm, auth.token?.sessionState!!)
        AuthenticationManager.backchannelLogout(session, userSession, true)
        return Response.noContent().build()
    }

    @POST
    @Path("user/sessions/logout")
    @Produces(MediaType.APPLICATION_JSON)
    fun logoutAll(): Response {
        val auth = ResourceAuthenticator.bearerAuth(session)
        if (!auth.hasScopeAccess("trust")) return ErrorHandler.forbidden()

        val currentSession = auth.token?.sessionState!!
        session.sessions().getUserSessionsStream(opexRealm, auth.user).toList().filter { it.id != currentSession }
            .forEach { AuthenticationManager.backchannelLogout(session, it, true) }

        return Response.noContent().build()
    }

    @POST
    @Path("user/sessions/{sessionId}/logout")
    @Produces(MediaType.APPLICATION_JSON)
    fun logout(@PathParam("sessionId") sessionId: String): Response {
        val auth = ResourceAuthenticator.bearerAuth(session)
        if (!auth.hasScopeAccess("trust")) return ErrorHandler.forbidden()

        val userSession = session.sessions().getUserSession(opexRealm, sessionId)
            ?: return ErrorHandler.notFound("Session not found")

        if (userSession.user.id != auth.getUserId()) return ErrorHandler.forbidden()

        AuthenticationManager.backchannelLogout(session, userSession, true)
        return Response.noContent().build()
    }

    @GET
    @Path("user/sessions")
    @Produces(MediaType.APPLICATION_JSON)
    fun getActiveSessions(): Response {
        val auth = ResourceAuthenticator.bearerAuth(session)
        if (!auth.hasScopeAccess("trust")) return ErrorHandler.forbidden()

        val user = session.users().getUserById(auth.getUserId(), opexRealm) ?: return ErrorHandler.userNotFound()
        val sessions = session.sessions().getUserSessionsStream(opexRealm, user)
            .filter { tryOrElse(null) { it.notes["agent"] } != "opex-admin" }.map {
                UserSessionResponse(
                    it.id,
                    it.ipAddress,
                    it.started.toLong(),
                    it.lastSessionRefresh.toLong(),
                    it.state?.name,
                    tryOrElse(null) { it.notes["agent"] },
                    auth.token?.sessionState == it.id
                )
            }.toList()

        return Response.ok(sessions).build()
    }

    private fun sendEmail(user: UserModel, sendAction: (EmailTemplateProvider) -> Unit) {
        if (!user.isEnabled) throw OpexException(OpexError.BadRequest, "User is disabled")

        val clientId = Constants.ACCOUNT_MANAGEMENT_CLIENT_ID
        val client = session.clients().getClientByClientId(opexRealm, clientId)
        if (client == null || !client.isEnabled) throw OpexException(OpexError.BadRequest, "Client error")

        try {
            val provider = session.getProvider(EmailTemplateProvider::class.java)
            sendAction(provider.setRealm(opexRealm).setUser(user))
        } catch (e: Exception) {
            logger.error("Unable to send verification email")
            e.printStackTrace()
        }
    }

    private fun sendUserEvent(user: UserModel) {
        val kafkaEvent = UserCreatedEvent(user.id, user.firstName, user.lastName, user.email!!)
        kafkaTemplate.send("auth_user_created", kafkaEvent)
        logger.info("$kafkaEvent produced in kafka topic")
    }

    private fun is2FAEnabled(user: UserModel): Boolean {
        return session.userCredentialManager().isConfiguredFor(opexRealm, user, OTPCredentialModel.TYPE)
    }

    private fun validateCaptcha(proof: String) {
        val client: HttpClient = HttpClientBuilder.create().build()
        val post = HttpGet(URIBuilder("http://captcha:8080/verify").addParameter("proof", proof).build())
        client.execute(post).let { response ->
            logger.info(response.statusLine.statusCode.toString())
            check(response.statusLine.statusCode / 500 != 5) { "Could not connect to Opex-Captcha service." }
            require(response.statusLine.statusCode / 100 == 2) { "Invalid captcha" }
        }
    }

    override fun close() {

    }

    override fun getResource(): Any {
        return this
    }
}
