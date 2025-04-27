package co.nilin.opex.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "keycloak")
class KeycloakConfig {
    lateinit var url: String
    lateinit var certUrl: String
    lateinit var realm: String
    lateinit var adminClient: Client
}

data class Client(
    val id: String,
    val secret: String,
    val googleClientId: String?
)
