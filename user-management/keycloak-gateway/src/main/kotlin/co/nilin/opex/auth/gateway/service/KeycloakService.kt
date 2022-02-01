package co.nilin.opex.auth.gateway.service

import co.nilin.opex.auth.gateway.data.RegisterUserRequest
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import org.keycloak.admin.client.CreatedResponseUtil
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.resource.RealmResource
import org.keycloak.representations.idm.UserRepresentation
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class KeycloakService(private val keycloak: Keycloak, private val opexRealm: RealmResource) {

    private val logger = LoggerFactory.getLogger(KeycloakService::class.java)

    /**
     * @return created user's id
     */
    fun registerUser(request: RegisterUserRequest): String {
        val userRep = UserRepresentation().apply {
            username = request.username
            email = request.email
            firstName = request.firstName
            lastName = request.lastName
            isEnabled = true
            isEmailVerified = false
            isTotp = false

            requiredActions = listOf("VERIFY_EMAIL", "UPDATE_PASSWORD")
            realmRoles = listOf("user")

            access = mapOf(
                "impersonate" to true,
                "manage" to true,
                "manageGroupMembership" to true,
                "mapRoles" to true,
                "view" to true,
            )
        }

        val response = opexRealm.users().create(userRep)
        val id = CreatedResponseUtil.getCreatedId(response)
        sendVerificationEmail(id)

        logger.info("User create response: status=${response.status} - id=$id")

        return id
    }

    fun forgotPassword(email: String?) {
        val userRep = opexRealm.users()
            .list()
            .find { it.email == email } ?: return

        val user = opexRealm.users().get(userRep.id) ?: return
        try {
            user.executeActionsEmail(listOf("UPDATE_EMAIL"))
        } catch (e: Exception) {
            logger.warn("forgotPassword: unable to send verification email")
            logger.error(e.message)
        }
    }

    fun sendVerification(email: String?) {
        val userRep = opexRealm.users()
            .list()
            .find { it.email == email } ?: return

        sendVerificationEmail(userRep.id)
    }

    private fun sendVerificationEmail(userId: String) {
        val user = opexRealm.users().get(userId)
        if (user.toRepresentation().isEmailVerified)
            throw OpexException(OpexError.EmailAlreadyVerified)

        try {
            user.sendVerifyEmail()
        } catch (e: Exception) {
            logger.warn("sendVerificationEmail: unable to send verification email")
            logger.error(e.message)
        }
    }

}