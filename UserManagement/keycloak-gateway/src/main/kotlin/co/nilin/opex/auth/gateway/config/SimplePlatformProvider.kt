package co.nilin.opex.auth.gateway.config

import org.keycloak.services.ServicesLogger

import org.keycloak.platform.PlatformProvider


class SimplePlatformProvider : PlatformProvider {
    var shutdownHook: Runnable? = null
    override fun onStartup(startupHook: Runnable) {
        startupHook.run()
    }

    override fun onShutdown(shutdownHook: Runnable) {
        this.shutdownHook = shutdownHook
    }

    override fun exit(cause: Throwable) {
        ServicesLogger.LOGGER.fatal(cause)
        exit(1)
    }

    private fun exit(status: Int) {
        object : Thread() {
            override fun run() {
                System.exit(status)
            }
        }.start()
    }
}