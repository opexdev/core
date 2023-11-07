package co.nilin.opex.auth.gateway.config

import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher
import org.jboss.resteasy.plugins.server.servlet.ResteasyContextParameters
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.resource.RealmResource
import org.keycloak.connections.jpa.updater.liquibase.ThreadLocalSessionContext
import org.keycloak.models.KeycloakSession
import org.keycloak.services.DefaultKeycloakSessionFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*
import javax.naming.*
import javax.naming.spi.InitialContextFactory
import javax.naming.spi.NamingManager
import javax.sql.DataSource

@Configuration
class EmbeddedKeycloakConfig {
    @Bean
    @Throws(Exception::class)
    fun keycloakJaxRsApplication(
        keycloakServerProperties: KeycloakServerProperties, dataSource: DataSource
    ): ServletRegistrationBean<HttpServlet30Dispatcher> {
        mockJndiEnvironment()
        EmbeddedKeycloakApplication.keycloakServerProperties = keycloakServerProperties
        val servlet = ServletRegistrationBean(
            HttpServlet30Dispatcher()
        )

        servlet.addInitParameter("javax.ws.rs.Application", EmbeddedKeycloakApplication::class.java.name)
        servlet.addInitParameter(
            ResteasyContextParameters.RESTEASY_SERVLET_MAPPING_PREFIX,
            keycloakServerProperties.contextPath
        )
        servlet.addInitParameter(ResteasyContextParameters.RESTEASY_USE_CONTAINER_FORM_PARAMS, "true")
        servlet.addUrlMappings(keycloakServerProperties.contextPath + "/*")
        servlet.setLoadOnStartup(1)
        servlet.isAsyncSupported = true
        return servlet
    }

    @Bean
    fun keycloakSessionManagement(
        keycloakServerProperties: KeycloakServerProperties
    ): FilterRegistrationBean<EmbeddedKeycloakRequestFilter> {
        val filter: FilterRegistrationBean<EmbeddedKeycloakRequestFilter> =
            FilterRegistrationBean<EmbeddedKeycloakRequestFilter>()
        filter.setName("Keycloak Session Management")
        filter.filter = EmbeddedKeycloakRequestFilter()
        filter.addUrlPatterns(keycloakServerProperties.contextPath + "/*")
        return filter
    }
    @Bean
    fun keycloakSession(): KeycloakSession? {
     return  ThreadLocalSessionContext.getCurrentSession();
    }




    @Throws(NamingException::class)
    private fun mockJndiEnvironment() {
        NamingManager.setInitialContextFactoryBuilder {
            InitialContextFactory { environment: Hashtable<*, *>? ->
                object : InitialContext() {
                    @Throws(NamingException::class)
                    override fun lookup(name: Name): Any? {
                        return lookup(name.toString())
                    }

                    @Throws(NamingException::class)
                    override fun lookup(name: String): Any? {
                        return Optional.ofNullable(environment!![name])
                            .orElseThrow { NamingException("Name $name not found") }
                    }

                    override fun getNameParser(name: String?): NameParser {
                        return NameParser { CompositeName() }
                    }
                }
            }
        }
    }
}
