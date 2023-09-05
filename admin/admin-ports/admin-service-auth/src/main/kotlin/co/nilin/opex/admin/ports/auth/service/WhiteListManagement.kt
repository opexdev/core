package co.nilin.opex.admin.ports.auth.service

import co.nilin.opex.admin.core.spi.WhiteListPersister
import co.nilin.opex.admin.ports.auth.proxy.KeycloakProxy
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.resource.RealmResource
import org.springframework.stereotype.Service

@Service
class WhiteListManagement(private val keycloak: Keycloak,
                          private val opexRealm: RealmResource,
                          private val proxy: KeycloakProxy) : WhiteListPersister {
    override fun addToWhiteList(users: List<String>) {
        keycloak
    }

    override fun deleteFromWhiteList(users: List<String>) {
        TODO("Not yet implemented")
    }

    override fun getWhiteList(): List<String>? {
        TODO("Not yet implemented")
    }
}