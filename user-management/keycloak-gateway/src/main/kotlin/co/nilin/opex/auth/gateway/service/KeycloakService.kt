package co.nilin.opex.auth.gateway.service

import co.nilin.opex.auth.gateway.data.RegisterUserRequest
import co.nilin.opex.auth.gateway.data.UserProfileInfo
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import org.keycloak.admin.client.CreatedResponseUtil
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.resource.RealmResource
import org.keycloak.models.UserModel
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

            requiredActions =
                listOf(UserModel.RequiredAction.VERIFY_EMAIL.name, UserModel.RequiredAction.UPDATE_PASSWORD.name)
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
            user.executeActionsEmail(listOf(UserModel.RequiredAction.UPDATE_PASSWORD.name))
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

    fun updateAttributes(userId: String, info: UserProfileInfo) {
        val user = opexRealm.users().get(userId) ?: throw OpexException(OpexError.Forbidden, "User not found")
        val userRep = user.toRepresentation()
        userRep.attributes.apply {
            with(info) {
                put("firstNameFa", listOf(firstNameFa))
                put("lastNameEn", listOf(lastNameEn))
                put("firstNameFa", listOf(firstNameFa))
                put("lastNameFa", listOf(lastNameFa))
                put("birthday", listOf(birthday))
                put("birthdayJalali", listOf(birthdayJalali))
                put("nationalID", listOf(nationalID))
                put("passport", listOf(passport))
                put("phoneNumber", listOf(phoneNumber))
                put("homeNumber", listOf(homeNumber))
                put("email", listOf(email))
                put("postalCode", listOf(postalCode))
                put("address", listOf(address))
            }
        }
        user.update(userRep)
    }

    fun getAttributes(userId: String): Map<String, List<String>> {
        val user = opexRealm.users().get(userId) ?: throw OpexException(OpexError.Forbidden, "User not found")
        return user.toRepresentation().attributes
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