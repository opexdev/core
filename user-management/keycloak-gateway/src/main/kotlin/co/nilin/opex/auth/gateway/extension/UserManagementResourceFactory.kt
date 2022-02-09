package co.nilin.opex.auth.gateway.extension

import org.keycloak.Config
import org.keycloak.models.KeycloakSession
import org.keycloak.models.KeycloakSessionFactory
import org.keycloak.services.resource.RealmResourceProvider
import org.keycloak.services.resource.RealmResourceProviderFactory

class UserManagementResourceFactory : RealmResourceProviderFactory {

    override fun create(session: KeycloakSession): RealmResourceProvider {
        return UserManagementResource(session)
    }

    override fun init(config: Config.Scope) {

    }

    override fun postInit(factory: KeycloakSessionFactory?) {

    }

    override fun close() {

    }

    override fun getId(): String {
        return "user-management"
    }
}