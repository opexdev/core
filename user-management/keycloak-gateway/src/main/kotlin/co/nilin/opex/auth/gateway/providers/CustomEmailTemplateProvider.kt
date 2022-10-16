package co.nilin.opex.auth.gateway.providers

import co.nilin.opex.auth.gateway.ApplicationContextHolder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.keycloak.email.EmailSenderProvider
import org.keycloak.email.EmailTemplateProvider
import org.keycloak.events.Event
import org.keycloak.models.KeycloakSession
import org.keycloak.models.RealmModel
import org.keycloak.models.UserModel
import org.keycloak.sessions.AuthenticationSessionModel
import org.springframework.core.io.ClassPathResource

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
        val template = processTemplate("password-reset.html") {
            getElementById("action-button")?.attr("href", link ?: "")
        }
        send("$appName reset password", template.html(), "Please click on the link below to reset password\n $link")
    }

    override fun sendSmtpTestEmail(config: MutableMap<String, String>?, user: UserModel?) {
        val template = processTemplate("execute-action.html") {
            getElementById("action-button")?.attr("href", "/where-link-goes")
        }
        send("$appName SMTP test email", template.html(), "This is a test email")
    }

    override fun sendConfirmIdentityBrokerLink(link: String?, expirationInMinutes: Long) {
        TODO("Not yet implemented")
    }

    override fun sendExecuteActions(link: String?, expirationInMinutes: Long) {
        val template = processTemplate("execute-action.html") {
            getElementById("action-button")?.attr("href", link ?: "")
        }
        send("$appName execute actions", template.html(), "Please click on the link below to execute actions\n $link")
    }

    override fun sendVerifyEmail(link: String?, expirationInMinutes: Long) {
        val template = processTemplate("verify-email.html") {
            getElementById("action-button")?.attr("href", link ?: "")
        }
        send("$appName verify email", template.html(), "Please click on the link below to verify email\n $link")
    }

    override fun send(subjectFormatKey: String?, bodyTemplate: String?, bodyAttributes: MutableMap<String, Any>?) {
        send(subjectFormatKey, mutableListOf(), bodyTemplate, bodyAttributes)
    }

    override fun send(
        subjectFormatKey: String?,
        subjectAttributes: MutableList<Any>?,
        bodyTemplate: String?,
        bodyAttributes: MutableMap<String, Any>?
    ) {

    }

    private fun send(subject: String, body: String, textBody: String) {
        val emailSender = session.getProvider(EmailSenderProvider::class.java)
        emailSender.send(realm?.smtpConfig, user, subject, textBody, body)
    }

    private fun processTemplate(fileName: String, docBuilder: Document.() -> Unit): Document {
        return getTemplateAsDocument(fileName).apply {
            getElementById("site")?.apply {
                attr("href", baseUrl)
                text(appName)
            }
            getElementById("link")?.apply {
                attr("href", baseUrl)
                text(appName)
            }
            docBuilder(this)
        }
    }

    override fun close() {}

    private fun getTemplate(fileName: String): String {
        return ClassPathResource("email-templates/$fileName").inputStream
            .bufferedReader()
            .use { it.readText() }
    }

    private fun getTemplateAsDocument(fileName: String): Document {
        return Jsoup.parse(getTemplate(fileName))
    }
}