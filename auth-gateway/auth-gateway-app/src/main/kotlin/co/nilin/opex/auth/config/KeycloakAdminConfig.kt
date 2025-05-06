package co.nilin.opex.auth.config

import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.resource.RealmResource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KeycloakAdminConfig {

    @Bean
    fun keycloak(config: KeycloakConfig): Keycloak {
        return Keycloak.getInstance(
            config.url,
            config.realm,
            config.adminClient.id,
            config.adminClient.secret,
        )
    }

    @Bean
    fun opexRealm(keycloak: Keycloak, config: KeycloakConfig): RealmResource {
        return keycloak.realm(config.realm)
    }
}