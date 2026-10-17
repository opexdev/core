package co.nilin.opex.auth.service

import co.nilin.opex.auth.data.PersonType
import co.nilin.opex.auth.data.UserCreatedEvent
import co.nilin.opex.auth.kafka.AuthEventProducer
import co.nilin.opex.auth.model.CreateLegalRepresentativeRequest
import co.nilin.opex.auth.model.CreateLegalRepresentativeResponse
import co.nilin.opex.auth.model.SendCredentialsResponse
import co.nilin.opex.auth.proxy.KeycloakProxy
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.LoggerDelegate
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.security.SecureRandom

@Service
class AdminUserService(
    private val keycloakProxy: KeycloakProxy,
    private val authProducer: AuthEventProducer,
    private val emailSender: EmailSender,
    @Value("\${app.name}")
    private val appName: String
) {
    private val logger by LoggerDelegate()

    private val template: String by lazy {
        ClassPathResource("templates/credentials-email-fa.html").inputStream
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
    }

    suspend fun createLegalRepresentative(
        request: CreateLegalRepresentativeRequest
    ): CreateLegalRepresentativeResponse {
        validate(request)

        val uuid = keycloakProxy.createLegalRepresentative(
            request.firstName,
            request.lastName,
            request.email,
            request.mobile
        )

        // Downstream modules build the profile and the wallet owner off this event
        authProducer.send(
            UserCreatedEvent(
                uuid = uuid,
                username = request.email,
                email = request.email,
                mobile = request.mobile,
                firstName = request.firstName,
                lastName = request.lastName,
                personType = PersonType.LEGAL
            )
        )

        return CreateLegalRepresentativeResponse(uuid)
    }

    suspend fun sendCredentials(uuid: String): SendCredentialsResponse {
        val user = keycloakProxy.findUserByUuid(uuid) ?: throw OpexError.UserNotFound.exception()
        val email = user.email ?: throw OpexError.BadRequest.exception("User has no email address")

        val password = generatePassword()
        // Flagged first so a failure here never leaves a usable generated password behind
        keycloakProxy.setMustChangePassword(uuid, true)
        keycloakProxy.resetPassword(uuid, password)

        val body = template
            .replace("{{PASSWORD}}", password)
            .replace("{{APP_NAME}}", appName)
        val emailSent = emailSender.send(email, "$appName account credentials", body)
        if (!emailSent) logger.error("Could not deliver credentials email of $uuid")

        return SendCredentialsResponse(emailSent)
    }

    suspend fun deleteUser(uuid: String) {
        keycloakProxy.deleteUser(uuid)
    }

    private fun validate(request: CreateLegalRepresentativeRequest) {
        val missing = buildList {
            if (request.firstName.isBlank()) add("firstName")
            if (request.lastName.isBlank()) add("lastName")
            if (request.email.isBlank()) add("email")
            if (request.mobile.isBlank()) add("mobile")
        }
        if (missing.isNotEmpty())
            throw OpexError.InvalidRequestBody.exception("Missing required fields: ${missing.joinToString()}")
    }

    private fun generatePassword(): String {
        val random = SecureRandom()
        return (1..PASSWORD_LENGTH).map { PASSWORD_ALPHABET[random.nextInt(PASSWORD_ALPHABET.size)] }.joinToString("")
    }

    private companion object {
        const val PASSWORD_LENGTH = 10
        val PASSWORD_ALPHABET = (('A'..'Z') + ('a'..'z') + ('0'..'9'))
    }
}
