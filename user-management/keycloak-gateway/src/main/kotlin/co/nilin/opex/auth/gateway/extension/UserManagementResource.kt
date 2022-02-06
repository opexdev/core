package co.nilin.opex.auth.gateway.extension

import co.nilin.opex.auth.gateway.data.RegisterUserRequest
import co.nilin.opex.auth.gateway.data.RegisterUserResponse
import co.nilin.opex.auth.gateway.data.UserProfileInfo
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import org.keycloak.authentication.actiontoken.execactions.ExecuteActionsActionToken
import org.keycloak.common.util.Time
import org.keycloak.email.EmailTemplateProvider
import org.keycloak.models.Constants
import org.keycloak.models.KeycloakSession
import org.keycloak.models.UserModel
import org.keycloak.services.resource.RealmResourceProvider
import org.keycloak.services.resources.LoginActionsService
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import kotlin.streams.toList

class UserManagementResource(private val session: KeycloakSession) : RealmResourceProvider {

    private val logger = LoggerFactory.getLogger(UserManagementResource::class.java)
    private val opexRealm = session.realms().getRealm("opex")

    @POST
    @Path("user")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun registerUser(request: RegisterUserRequest): Response {
        if (!request.isValid())
            throw OpexException(OpexError.BadRequest)

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
        return Response.ok(RegisterUserResponse(user.id)).build()
    }

    @POST
    @Path("user/forgot")
    @Produces(MediaType.APPLICATION_JSON)
    fun forgotPassword(@QueryParam("email") email: String?): Response {
        ResourceAuthenticator.bearerAuth(session).checkAccess("trust")
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
        auth.checkScopeAccess("trust")
        val user = session.users().getUserByEmail(email, opexRealm)
        if (user != null) {
            auth.checkUserAccess(user.id)
            sendEmail(user, listOf(UserModel.RequiredAction.VERIFY_EMAIL.name))
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

    override fun close() {

    }

    override fun getResource(): Any {
        return this
    }
}