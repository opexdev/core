package co.nilin.opex.auth.gateway.authenticator

import co.nilin.opex.auth.gateway.utils.ErrorHandler
import co.nilin.opex.common.OpexError
import org.keycloak.authentication.AuthenticationFlowContext
import org.keycloak.authentication.AuthenticationFlowError
import org.keycloak.authentication.CredentialValidator
import org.keycloak.authentication.authenticators.directgrant.AbstractDirectGrantAuthenticator
import org.keycloak.credential.CredentialProvider
import org.keycloak.credential.OTPCredentialProvider
import org.keycloak.events.Errors
import org.keycloak.models.*
import org.keycloak.models.credential.OTPCredentialModel
import org.keycloak.provider.ProviderConfigProperty
import javax.ws.rs.core.Response

class CustomOTPAuthenticator : AbstractDirectGrantAuthenticator(), CredentialValidator<OTPCredentialProvider> {

    override fun authenticate(context: AuthenticationFlowContext) {
        val session = context.session
        val realm = context.realm
        val user = context.user

        if (!configuredFor(session, context.realm, user)) {
            if (context.execution.isConditional) {
                context.attempted()
            } else if (context.execution.isRequired) {
                context.event.error(Errors.INVALID_USER_CREDENTIALS)
                val challengeResponse = ErrorHandler.response(Response.Status.BAD_REQUEST, OpexError.OTPRequired)
                context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse)
            }
            return
        }

        val inputData = context.httpRequest.decodedFormParameters
        val otp = inputData.getFirst("otp") ?: inputData.getFirst("totp")


        if (otp == null) {
            user?.let { context.event.user(it) }
            val response = ErrorHandler.response(Response.Status.BAD_REQUEST, OpexError.OTPRequired)
            context.failure(AuthenticationFlowError.INVALID_USER, response)
            return
        }

        val credentialId = getCredentialProvider(session).getDefaultCredential(session, realm, user).id
        val isValid = getCredentialProvider(session)
            .isValid(realm, user, UserCredentialModel(credentialId, OTPCredentialModel.TYPE, otp))

        if (!isValid) {
            context.event.user(user)
            context.event.error(Errors.INVALID_USER_CREDENTIALS)
            val response = ErrorHandler.response(Response.Status.FORBIDDEN, OpexError.InvalidOTP)
            context.failure(AuthenticationFlowError.INVALID_USER, response)
            return
        }

        context.success()
    }

    override fun requiresUser(): Boolean {
        return true
    }

    override fun configuredFor(session: KeycloakSession, realm: RealmModel?, user: UserModel?): Boolean {
        return getCredentialProvider(session).isConfiguredFor(realm, user)
    }

    override fun setRequiredActions(session: KeycloakSession?, realm: RealmModel?, user: UserModel?) {

    }

    override fun getId(): String {
        return "direct-grant-validate-otp-custom"
    }

    override fun getHelpText(): String {
        return "Custom OTP validator"
    }

    override fun getDisplayType(): String {
        return "Custom OTP"
    }

    override fun getReferenceCategory(): String? {
        return null
    }

    override fun getConfigProperties(): MutableList<ProviderConfigProperty> {
        return mutableListOf()
    }

    override fun isConfigurable(): Boolean {
        return false
    }

    override fun getRequirementChoices(): Array<AuthenticationExecutionModel.Requirement> {
        return REQUIREMENT_CHOICES
    }

    override fun isUserSetupAllowed(): Boolean {
        return false
    }

    override fun getCredentialProvider(session: KeycloakSession): OTPCredentialProvider {
        return session.getProvider(CredentialProvider::class.java, "keycloak-otp") as OTPCredentialProvider
    }

}