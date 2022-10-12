package co.nilin.opex.auth.gateway.providers

import org.keycloak.Config
import org.keycloak.email.EmailTemplateProvider
import org.keycloak.email.EmailTemplateProviderFactory
import org.keycloak.models.KeycloakSession
import org.keycloak.models.KeycloakSessionFactory

class CustomEmailTemplateProviderFactory : EmailTemplateProviderFactory {

    override fun create(session: KeycloakSession): EmailTemplateProvider {
        return CustomEmailTemplateProvider(session)
    }

    override fun init(config: Config.Scope?) {

    }

    override fun postInit(factory: KeycloakSessionFactory?) {

    }

    override fun close() {

    }

    override fun getId(): String {
        return "custom-email-template"
    }
}