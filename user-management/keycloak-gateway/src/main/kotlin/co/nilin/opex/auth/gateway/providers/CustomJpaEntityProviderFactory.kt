package co.nilin.opex.auth.gateway.providers

import org.keycloak.Config
import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider
import org.keycloak.connections.jpa.entityprovider.JpaEntityProviderFactory
import org.keycloak.models.KeycloakSession
import org.keycloak.models.KeycloakSessionFactory

class CustomJpaEntityProviderFactory : JpaEntityProviderFactory {
    override fun create(session: KeycloakSession?): JpaEntityProvider {
        return CustomJpaProvider();
    }

    override fun init(config: Config.Scope?) {
    }

    override fun postInit(factory: KeycloakSessionFactory?) {
    }

    override fun close() {
    }

    override fun getId(): String {
        return "custom-jpa-provider"
    }
}