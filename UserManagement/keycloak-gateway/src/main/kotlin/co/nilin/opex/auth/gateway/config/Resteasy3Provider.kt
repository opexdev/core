package co.nilin.opex.auth.gateway.config

import org.jboss.resteasy.core.Dispatcher
import org.jboss.resteasy.spi.ResteasyProviderFactory
import org.keycloak.common.util.ResteasyProvider

class Resteasy3Provider: ResteasyProvider {
    override fun <R> getContextData(type: Class<R>?): R {
        return ResteasyProviderFactory.getContextData(type)
    }

    override fun pushDefaultContextObject(type: Class<*>?, instance: Any?) {
        getContextData(Dispatcher::class.java).defaultContextObjects[type] =
            instance
    }

    override fun pushContext(type: Class<Any>, instance: Any) {
        ResteasyProviderFactory.pushContext<Any>(type, instance)
    }

    override fun clearContextData() {
        ResteasyProviderFactory.clearContextData()
    }
}