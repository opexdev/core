package co.nilin.opex.auth.gateway.extension

import co.nilin.opex.auth.gateway.ApplicationContextHolder
import co.nilin.opex.auth.gateway.data.RegisterUserRequest
import co.nilin.opex.auth.gateway.data.RegisterUserResponse
import co.nilin.opex.auth.gateway.data.UserProfileInfo
import co.nilin.opex.auth.gateway.model.AuthEvent
import co.nilin.opex.auth.gateway.model.UserCreatedEvent
import co.nilin.opex.auth.gateway.utils.ErrorHandler
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
import org.keycloak.models.UserModel
import org.keycloak.services.resource.RealmResourceProvider
import org.keycloak.services.resources.LoginActionsService
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import java.util.concurrent.TimeUnit
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import kotlin.streams.toList

class UserManagementResource(private val session: KeycloakSession) : RealmResourceProvider {

    private val logger = LoggerFactory.getLogger(UserManagementResource::class.java)
    private val opexRealm = session.realms().getRealm("opex")
    private val kafkaTemplate by lazy {
        ApplicationContextHolder.getCurrentContext()!!
            .getBean("authKafkaTemplate") as KafkaTemplate<String, AuthEvent>
    }

    @POST
    @Path("user")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun registerUser(request: RegisterUserRequest): Response {
        val auth = ResourceAuthenticator.bearerAuth(session)
        if (!auth.hasScopeAccess("trust"))
            return ErrorHandler.forbidden()

        if (!request.isValid())
            return ErrorHandler.response(Response.Status.BAD_REQUEST, OpexException(OpexError.BadRequest))

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
        @QueryParam("captcha-answer") captchaAnswer: String
    ): Response {
        val client: HttpClient = HttpClientBuilder.create().build()
        val post = HttpGet(URIBuilder("https://captcha:8080").addParameter("proof", captchaAnswer).build())
        client.execute(post).let { response ->
            check(response.statusLine.statusCode / 500 != 5) { "Could not connect to Opex-Captcha service." }
            require(response.statusLine.statusCode / 100 == 2) { "Invalid captcha" }
        }

        val auth = ResourceAuthenticator.bearerAuth(session)
        if (!auth.hasScopeAccess("trust"))
            return ErrorHandler.forbidden()

        val user = session.users().getUserByEmail(email, opexRealm)
        if (user != null) {
            sendEmail(user, listOf(UserModel.RequiredAction.UPDATE_PASSWORD.name))
        }
        return Response.noContent().build()
    }

    @POST
    @Path("user/verify-email")
    @Produces(MediaType.APPLICATION_JSON)
    fun sendVerifyEmail(@QueryParam("email") email: String?): Response {
        val auth = ResourceAuthenticator.bearerAuth(session)
        if (!auth.hasScopeAccess("trust"))
            return ErrorHandler.forbidden()

        val user = session.users().getUserByEmail(email, opexRealm)
        if (user != null) {
            if (!auth.hasUserAccess(user.id))
                return ErrorHandler.forbidden()

            sendEmail(user, listOf(UserModel.RequiredAction.VERIFY_EMAIL.name))
        }
        return Response.noContent().build()
    }

    @GET
    @Path("user/profile")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun getAttributes(): Response {
        val auth = ResourceAuthenticator.bearerAuth(session)
        if (!auth.hasScopeAccess("trust"))
            return ErrorHandler.forbidden()

        val user = session.users().getUserById(auth.getUserId(), opexRealm)
            ?: return ErrorHandler.response(
                Response.Status.NOT_FOUND,
                OpexException(OpexError.NotFound, "User not found")
            )

        return Response.ok(user.attributes).build()
    }

    @POST
    @Path("user/profile")
    @Consumes(MediaType.APPLICATION_JSON)
    fun updateAttributes(request: UserProfileInfo): Response {
        val auth = ResourceAuthenticator.bearerAuth(session)
        if (!auth.hasScopeAccess("trust"))
            return ErrorHandler.forbidden()

        val user = session.users().getUserById(auth.getUserId(), opexRealm)
            ?: return ErrorHandler.response(
                Response.Status.NOT_FOUND,
                OpexException(OpexError.NotFound, "User not found")
            )

        with(request) {
            user.setSingleAttribute("firstNameFa", firstNameFa)
            user.setSingleAttribute("lastNameEn", lastNameEn)
            user.setSingleAttribute("firstNameFa", firstNameFa)
            user.setSingleAttribute("lastNameFa", lastNameFa)
            user.setSingleAttribute("birthday", birthday)
            user.setSingleAttribute("birthdayJalali", birthdayJalali)
            user.setSingleAttribute("nationalID", nationalID)
            user.setSingleAttribute("passport", passport)
            user.setSingleAttribute("phoneNumber", phoneNumber)
            user.setSingleAttribute("homeNumber", homeNumber)
            user.setSingleAttribute("email", email)
            user.setSingleAttribute("postalCode", postalCode)
            user.setSingleAttribute("address", address)
        }

        return Response.noContent().build()
    }

    private fun sendEmail(user: UserModel, actions: List<String>) {
        if (!user.isEnabled)
            throw OpexException(OpexError.BadRequest, "User is disabled")

        val clientId = Constants.ACCOUNT_MANAGEMENT_CLIENT_ID
        val client = session.clients().getClientByClientId(opexRealm, clientId)
        if (client == null || !client.isEnabled)
            throw OpexException(OpexError.BadRequest, "Client error")

        val lifespan = opexRealm.actionTokenGeneratedByAdminLifespan
        val expiration = Time.currentTime() + lifespan
        val token = ExecuteActionsActionToken(user.id, expiration, actions, null, clientId)

        try {
            val provider = session.getProvider(EmailTemplateProvider::class.java)
            val builder = LoginActionsService.actionTokenProcessor(session.context.uri).apply {
                queryParam("key", token.serialize(session, opexRealm, session.context.uri))
            }
            val link = builder.build(opexRealm.name).toString()
            provider.setRealm(opexRealm)
                .setUser(user)
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

    override fun close() {

    }

    override fun getResource(): Any {
        return this
    }
}
