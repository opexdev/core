package co.nilin.opex.auth.gateway.extension

import org.apache.http.client.methods.HttpGet
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.CloseableHttpClient
import org.keycloak.Config
import org.keycloak.authentication.FormAction
import org.keycloak.authentication.FormActionFactory
import org.keycloak.authentication.FormContext
import org.keycloak.authentication.ValidationContext
import org.keycloak.connections.httpclient.HttpClientProvider
import org.keycloak.events.Details
import org.keycloak.events.Errors
import org.keycloak.forms.login.LoginFormsProvider
import org.keycloak.models.*
import org.keycloak.models.utils.FormMessage
import org.keycloak.provider.ConfiguredProvider
import org.keycloak.provider.ProviderConfigProperty
import org.keycloak.services.ServicesLogger
import org.keycloak.services.validation.Validation

class RegistrationOpexCaptcha : FormAction, FormActionFactory, ConfiguredProvider {
    override fun getDisplayType(): String {
        return "Opex-Captcha"
    }

    override fun getReferenceCategory(): String? {
        return null
    }

    override fun isConfigurable(): Boolean {
        return false
    }

    override fun getRequirementChoices(): Array<AuthenticationExecutionModel.Requirement> {
        return REQUIREMENT_CHOICES
    }

    override fun buildPage(context: FormContext, form: LoginFormsProvider) {}

    override fun validate(context: ValidationContext) {
        val formData = context.httpRequest.decodedFormParameters
        val errors: MutableList<FormMessage> = ArrayList()
        var success = false
        context.event.detail(Details.REGISTER_METHOD, "form")
        val captcha = formData.getFirst(CAPTCHA_ANSWER)
        if (!Validation.isBlank(captcha)) {
            success = validateOpexCaptcha(context, captcha)
        }
        if (success) {
            context.success()
        } else {
            errors.add(FormMessage(null, "opexCaptchaFailed"))
            formData.remove(CAPTCHA_ANSWER)
            context.error(Errors.INVALID_REGISTRATION)
            context.validationError(formData, errors)
            context.excludeOtherErrors()
        }
    }

    override fun success(context: FormContext) {}

    override fun requiresUser(): Boolean {
        return false
    }

    override fun configuredFor(session: KeycloakSession, realm: RealmModel, user: UserModel): Boolean {
        return true
    }

    override fun setRequiredActions(session: KeycloakSession, realm: RealmModel, user: UserModel) {}

    override fun isUserSetupAllowed(): Boolean {
        return false
    }

    override fun close() {}

    override fun create(session: KeycloakSession): FormAction {
        return this
    }

    override fun init(config: Config.Scope) {}

    override fun postInit(factory: KeycloakSessionFactory) {}

    override fun getId(): String {
        return PROVIDER_ID
    }

    override fun getHelpText(): String {
        return "Enables Opex-Captcha test."
    }

    override fun getConfigProperties(): List<ProviderConfigProperty> {
        return ArrayList()
    }

    private fun validateOpexCaptcha(context: ValidationContext, captcha: String?): Boolean {
        var success = false
        try {
            val httpClient = context.session.getProvider(
                HttpClientProvider::class.java
            ).httpClient as CloseableHttpClient
            val xForwardedFor = context.httpRequest.httpHeaders.getRequestHeader("X-Forwarded-For")
            val proof = "$captcha-${xForwardedFor.first()}"
            val post = HttpGet(URIBuilder("http://captcha:8080").addParameter("proof", proof).build())
            httpClient.execute(post).use { response ->
                check(response.statusLine.statusCode / 500 != 5) { "Could not connect to Opex-Captcha service." }
                success = response.statusLine.statusCode / 100 == 2
            }
        } catch (e: Exception) {
            ServicesLogger.LOGGER.error(e)
        }
        return success
    }

    companion object {
        const val CAPTCHA_ANSWER = "captcha-answer"
        const val PROVIDER_ID = "registration-opex-captcha-action"
        private val REQUIREMENT_CHOICES = arrayOf(
            AuthenticationExecutionModel.Requirement.REQUIRED, AuthenticationExecutionModel.Requirement.DISABLED
        )
    }
}
