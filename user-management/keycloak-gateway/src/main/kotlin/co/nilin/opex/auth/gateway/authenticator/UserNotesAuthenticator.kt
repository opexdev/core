package co.nilin.opex.auth.gateway.authenticator

import co.nilin.opex.auth.gateway.utils.ErrorHandler
import co.nilin.opex.utility.error.data.OpexError
import org.keycloak.authentication.AuthenticationFlowContext
import org.keycloak.authentication.AuthenticationFlowError
import org.keycloak.authentication.authenticators.directgrant.AbstractDirectGrantAuthenticator
import org.keycloak.models.AuthenticationExecutionModel
import org.keycloak.models.KeycloakSession
import org.keycloak.models.RealmModel
import org.keycloak.models.UserModel
import org.keycloak.provider.ProviderConfigProperty
import javax.ws.rs.core.Response

class UserNotesAuthenticator : AbstractDirectGrantAuthenticator() {

    override fun authenticate(context: AuthenticationFlowContext) {
        if (context.execution.isDisabled) {
            context.attempted()
            return
        }

        //TODO add configurable parameters with validation
        val inputData = context.httpRequest.decodedFormParameters
        val agent = inputData.getFirst("agent")
        if (agent.isNullOrEmpty()) {
            val response = ErrorHandler.response(
                Response.Status.BAD_REQUEST,
                OpexError.BadRequest,
                "Parameter 'agent' required but not found"
            )
            context.failure(AuthenticationFlowError.INTERNAL_ERROR, response)
            return
        }

        context.authenticationSession.setUserSessionNote("agent", agent)
        context.success()
    }

    override fun requiresUser(): Boolean {
        return false
    }

    override fun configuredFor(session: KeycloakSession, realm: RealmModel?, user: UserModel?): Boolean {
        return true
    }

    override fun setRequiredActions(session: KeycloakSession?, realm: RealmModel?, user: UserModel?) {

    }

    override fun getId(): String {
        return "user-notes-validate"
    }

    override fun getHelpText(): String {
        return "User session notes validator"
    }

    override fun getDisplayType(): String {
        return "User session note validator"
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
        return arrayOf(
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
        )
    }

    override fun isUserSetupAllowed(): Boolean {
        return false
    }
}