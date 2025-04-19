package co.nilin.opex.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "keycloak")
class KeycloakConfig {
    lateinit var url: String
    lateinit var realm: String
    lateinit var clientId: String
    lateinit var clientSecret: String
    lateinit var googleClientId: String
} 