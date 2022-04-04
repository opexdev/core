package co.nilin.opex.auth.gateway.extension

import co.nilin.opex.auth.gateway.ApplicationContextHolder
import co.nilin.opex.auth.gateway.data.*
import co.nilin.opex.auth.gateway.model.AuthEvent
import co.nilin.opex.auth.gateway.model.UserCreatedEvent
import co.nilin.opex.auth.gateway.utils.ErrorHandler
import co.nilin.opex.auth.gateway.utils.OTPUtils
import co.nilin.opex.auth.gateway.utils.ResourceAuthenticator
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
import org.keycloak.models.utils.HmacOTP
import org.keycloak.services.resource.RealmResourceProvider
import org.keycloak.services.resources.LoginActionsService
import org.keycloak.utils.CredentialHelper
import org.keycloak.utils.TotpUtils
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.RequestHeader
import java.util.concurrent.TimeUnit
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import kotlin.streams.toList

class UserManagementResource(private val session: KeycloakSession) : RealmResourceProvider {

    private val logger = LoggerFactory.getLogger(UserManagementResource::class.java)
    private val opexRealm = session.realms().getRealm("opex")
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

        if (!request.isValid()) return ErrorHandler.response(Response.Status.BAD_REQUEST, OpexError.BadRequest)

        val user = session.users().addUser(opexRealm, request.username).apply {
            email = request.email
            firstName = request.firstName
            lastName = request.lastName
            isEnabled = true
            isEmailVerified = false

            addRequiredAction(UserModel.RequiredAction.VERIFY_EMAIL)
            addRequiredAction(UserModel.RequiredAction.UPDATE_PASSWORD)
            sendEmail(this, requiredActionsStream.toList())
        }

        logger.info("User create response ${user.id}")
        sendUserEvent(user)

        return Response.ok(RegisterUserResponse(user.id)).build()
    }

    @POST
    @Path("user/forgot")
    @Produces(MediaType.APPLICATION_JSON)
    fun forgotPassword(
        @QueryParam("email") email: String?,
        @QueryParam("captcha-answer") captchaAnswer: String,
        @RequestHeader("X-Forwarded-For", defaultValue = "0.0.0.0") xForwardedFor: List<String>
    ): Response {
        val client: HttpClient = HttpClientBuilder.create().build()
        val proof = "$captchaAnswer-${xForwardedFor.first()}"
        val post = HttpGet(URIBuilder("http://captcha:8080").addParameter("proof", proof).build())
        client.execute(post).let { response ->
            runCatching {
                check(response.statusLine.statusCode / 500 != 5) { "Could not connect to Opex-Captcha service." }
                require(response.statusLine.statusCode / 100 == 2) { "Invalid captcha" }
            }.onFailure {
                return Response.status(Response.Status.BAD_REQUEST).build()
            }
        }

        val auth = ResourceAuthenticator.bearerAuth(session)
        if (!auth.hasScopeAccess("trust")) return ErrorHandler.forbidden()

        val user = session.users().getUserByEmail(email, opexRealm)
        if (user != null) {
            sendEmail(user, listOf(UserModel.RequiredAction.UPDATE_PASSWORD.name))
        }
        return Response.noContent().build()
    }

    @POST
    @Path("user/verify-email")
    @Produces(MediaType.APPLICATION_JSON)
    fun sendVerifyEmail(): Response {
        val auth = ResourceAuthenticator.bearerAuth(session)
        if (!auth.hasScopeAccess("trust")) return ErrorHandler.forbidden()

        val user = session.users().getUserById(auth.getUserId(), opexRealm) ?: return ErrorHandler.userNotFound()
        sendEmail(user, listOf(UserModel.RequiredAction.VERIFY_EMAIL.name))
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
        if (!session.userCredentialManager()
                .isValid(opexRealm, user, cred)
        ) return ErrorHandler.response(Response.Status.FORBIDDEN, OpexError.Forbidden, "Incorrect password")

        if (body.confirmation == body.newPassword) return ErrorHandler.response(
            Response.Status.BAD_REQUEST, OpexError.BadRequest, "Invalid password confirmation"
        )

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

    @GET
    @Path("user/sessions")
    @Produces(MediaType.APPLICATION_JSON)
    fun getActiveSessions(): Response {
        val auth = ResourceAuthenticator.bearerAuth(session)
        if (!auth.hasScopeAccess("trust")) return ErrorHandler.forbidden()

        val user = session.users().getUserById(auth.getUserId(), opexRealm) ?: return ErrorHandler.userNotFound()
        val sessions = session.sessions().getUserSessionsStream(opexRealm, user)
            .map { UserSessionResponse(it.ipAddress, it.started, it.lastSessionRefresh, it.state.name) }.toList()

        return Response.ok(sessions).build()
    }

    private fun sendEmail(user: UserModel, actions: List<String>) {
        if (!user.isEnabled) throw OpexException(OpexError.BadRequest, "User is disabled")

        val clientId = Constants.ACCOUNT_MANAGEMENT_CLIENT_ID
        val client = session.clients().getClientByClientId(opexRealm, clientId)
        if (client == null || !client.isEnabled) throw OpexException(OpexError.BadRequest, "Client error")

        val lifespan = opexRealm.actionTokenGeneratedByAdminLifespan
        val expiration = Time.currentTime() + lifespan
        val token = ExecuteActionsActionToken(user.id, expiration, actions, null, clientId)

        try {
            val provider = session.getProvider(EmailTemplateProvider::class.java)
            val builder = LoginActionsService.actionTokenProcessor(session.context.uri).apply {
                queryParam("key", token.serialize(session, opexRealm, session.context.uri))
            }
            val link = builder.build(opexRealm.name).toString()
            provider.setRealm(opexRealm).setUser(user)
                .sendVerifyEmail(link, TimeUnit.SECONDS.toMinutes(lifespan.toLong()))
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

    override fun close() {

    }

    override fun getResource(): Any {
        return this
    }
}
