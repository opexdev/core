package co.nilin.opex.admin.ports.auth.service

import co.nilin.opex.admin.core.data.WhitelistAdaptor
import co.nilin.opex.admin.core.spi.WhiteListPersister
import co.nilin.opex.admin.ports.auth.proxy.KeycloakProxy
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.resource.RealmResource
import org.springframework.stereotype.Service

@Service
class WhiteListService(private val keycloak: Keycloak,
                       private val opexRealm: RealmResource,
                       private val proxy: KeycloakProxy) : WhiteListPersister {
    override suspend fun addToWhiteList(users: WhitelistAdaptor): WhitelistAdaptor? {
        return proxy.addToWhiteList(users)
    }

    override suspend fun deleteFromWhiteList(users: WhitelistAdaptor): WhitelistAdaptor? {
        return proxy.deleteFromWhitelist(users)

    }

    override suspend fun getWhiteList(): WhitelistAdaptor? {
        return proxy.getWhiteListedUsers()
    }
}