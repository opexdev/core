package co.nilin.opex.auth.gateway.config

import org.keycloak.platform.PlatformProvider
import org.keycloak.services.ServicesLogger
import kotlin.system.exitProcess


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
                exitProcess(status)
            }
        }.start()
    }
}
