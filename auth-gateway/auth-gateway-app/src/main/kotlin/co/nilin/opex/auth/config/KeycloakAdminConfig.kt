package co.nilin.opex.auth.config

import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.admin.client.resource.RealmResource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KeycloakAdminConfig {

    @Bean
    fun keycloak(config: KeycloakConfig): Keycloak {
        return KeycloakBuilder.builder()
            .serverUrl(config.url)
            .realm(config.realm)
            .clientId(config.adminClient.id)
            .clientSecret(config.adminClient.secret)
            .grantType("client_credentials")
            .build()
    }

    @Bean
    fun opexRealm(keycloak: Keycloak, config: KeycloakConfig): RealmResource {
        return keycloak.realm(config.realm)
    }
}