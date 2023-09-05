package co.nilin.opex.auth.gateway.providers

import co.nilin.opex.auth.gateway.model.WhiteListModel
import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider

class CustomJpaProvider : JpaEntityProvider {
    override fun close() {
    }

    override fun getEntities(): MutableList<Class<*>> {
        return mutableListOf(WhiteListModel::class.java)

    }

    override fun getChangelogLocation(): String {
        return "META-INF/whitelisttt-changelog.xml"
    }

    override fun getFactoryId(): String {
        return "whitelist-entity"
    }
}