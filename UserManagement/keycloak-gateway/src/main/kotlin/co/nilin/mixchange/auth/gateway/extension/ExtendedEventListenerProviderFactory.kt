package co.nilin.mixchange.auth.gateway.extension

import org.keycloak.Config
import org.keycloak.events.EventListenerProviderFactory
import org.keycloak.models.KeycloakSession
import org.keycloak.models.KeycloakSessionFactory

class ExtendedEventListenerProviderFactory : EventListenerProviderFactory {
    override fun create(keycloakSession: KeycloakSession): ExtendedEventListenerProvider {
        return ExtendedEventListenerProvider(keycloakSession)
    }

    override fun init(scope: Config.Scope) {
        //
    }

    override fun postInit(keycloakSessionFactory: KeycloakSessionFactory) {
        //
    }

    override fun close() {
        //
    }

    override fun getId(): String {
        return "pl_event_listener"
    }
}