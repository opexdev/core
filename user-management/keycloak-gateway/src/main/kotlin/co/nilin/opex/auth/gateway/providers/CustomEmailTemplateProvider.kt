package co.nilin.opex.auth.gateway.providers

import co.nilin.opex.auth.gateway.ApplicationContextHolder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.keycloak.email.EmailTemplateProvider
import org.keycloak.events.Event
import org.keycloak.models.KeycloakSession
import org.keycloak.models.RealmModel
import org.keycloak.models.UserModel
import org.keycloak.sessions.AuthenticationSessionModel
import org.springframework.util.ResourceUtils
import java.io.File

class CustomEmailTemplateProvider(private val session: KeycloakSession) : EmailTemplateProvider {

    private var authenticationSession: AuthenticationSessionModel? = null
    private var realm: RealmModel? = null
    private var user: UserModel? = null
    private var attributes = HashMap<String, Any>()
    private val appName by lazy {
        ApplicationContextHolder.getCurrentContext()!!.environment.resolvePlaceholders("\${APP_NAME}")
    }
    private val baseUrl by lazy {
        ApplicationContextHolder.getCurrentContext()!!.environment.resolvePlaceholders("\${APP_BASE_URL}")
    }

    override fun setAuthenticationSession(authenticationSession: AuthenticationSessionModel?): EmailTemplateProvider {
        this.authenticationSession = authenticationSession
        return this
    }

    override fun setRealm(realm: RealmModel?): EmailTemplateProvider {
        this.realm = realm
        return this
    }

    override fun setUser(user: UserModel?): EmailTemplateProvider {
        this.user = user
        return this
    }

    override fun setAttribute(name: String?, value: Any?): EmailTemplateProvider {
        if (name != null && value != null)
            attributes[name] = value
        return this
    }

    override fun sendEvent(event: Event?) {
        TODO("Not yet implemented")
    }

    override fun sendPasswordReset(link: String?, expirationInMinutes: Long) {
        val template = getTemplateAsDocument("verify-email.html").apply {
            getElementById("name")?.attr("value", appName)
            getElementById("baseUrl")?.attr("value", baseUrl)
        }
    }

    override fun sendSmtpTestEmail(config: MutableMap<String, String>?, user: UserModel?) {
        TODO("Not yet implemented")
    }

    override fun sendConfirmIdentityBrokerLink(link: String?, expirationInMinutes: Long) {
        TODO("Not yet implemented")
    }

    override fun sendExecuteActions(link: String?, expirationInMinutes: Long) {
        val template = getTemplateAsDocument("verify-email.html").apply {
            getElementById("name")?.attr("value", appName)
            getElementById("baseUrl")?.attr("value", baseUrl)
        }
    }

    override fun sendVerifyEmail(link: String?, expirationInMinutes: Long) {
        val template = getTemplateAsDocument("verify-email.html").apply {
            getElementById("name")?.attr("value", appName)
            getElementById("baseUrl")?.attr("value", baseUrl)
        }
    }

    override fun send(subjectFormatKey: String?, bodyTemplate: String?, bodyAttributes: MutableMap<String, Any>?) {
        TODO("Not yet implemented")
    }

    override fun send(
        subjectFormatKey: String?,
        subjectAttributes: MutableList<Any>?,
        bodyTemplate: String?,
        bodyAttributes: MutableMap<String, Any>?
    ) {
        TODO("Not yet implemented")
    }

    override fun close() {}

    private fun getTemplate(fileName: String): File {
        return ResourceUtils.getFile("email-templates/$fileName")
    }

    private fun getTemplateAsDocument(fileName: String): Document {
        return Jsoup.parse(getTemplate(fileName))
    }
}