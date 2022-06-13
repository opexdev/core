package co.nilin.opex.auth.gateway.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "keycloak.server")
class KeycloakServerProperties {
    var contextPath = ""
    var realmImportFile = "/opex-realm.json"
    var adminUser = AdminUser()

    class AdminUser {
        var username = "admin"
        var password = "admin"
    }
}

