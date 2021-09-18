package co.nilin.opex.auth.gateway

import org.springframework.context.ApplicationContext

class ApplicationContextHolder {
    companion object {
        var applicationContext: ApplicationContext? = null
        fun getCurrentContext(): ApplicationContext? { return applicationContext }
        fun setCurrentContext(applicationContext: ApplicationContext) { Companion.applicationContext = applicationContext }
    }
}