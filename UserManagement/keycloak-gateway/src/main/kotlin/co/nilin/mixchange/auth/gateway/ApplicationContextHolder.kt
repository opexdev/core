package co.nilin.mixchange.auth.gateway

import org.springframework.context.ApplicationContext

class ApplicationContextHolder {
    companion object {
        var applicationContext: ApplicationContext? = null
        fun getCurrentContext(): ApplicationContext? { return applicationContext }
        fun setCurrentContext(applicationContext: ApplicationContext) { this.applicationContext = applicationContext }
    }
}