package co.nilin.mixchange.auth.gateway.config

import org.keycloak.common.ClientConnection
import org.keycloak.models.KeycloakSession
import org.keycloak.services.filters.AbstractRequestFilter
import java.io.UnsupportedEncodingException
import java.lang.Exception
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest


class EmbeddedKeycloakRequestFilter : AbstractRequestFilter(), Filter {
    @Throws(UnsupportedEncodingException::class)
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse?, filterChain: FilterChain) {
        servletRequest.setCharacterEncoding("UTF-8")
        val clientConnection = createConnection(servletRequest as HttpServletRequest)
        filter(clientConnection) { session: KeycloakSession? ->
            try {
                filterChain.doFilter(servletRequest, servletResponse)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }

    private fun createConnection(request: HttpServletRequest): ClientConnection {
        return object : ClientConnection {
            override fun getRemoteAddr(): String {
                return request.getRemoteAddr()
            }

            override fun getRemoteHost(): String {
                return request.getRemoteHost()
            }

            override fun getRemotePort(): Int {
                return request.getRemotePort()
            }

            override fun getLocalAddr(): String {
                return request.getLocalAddr()
            }

            override fun getLocalPort(): Int {
                return request.getLocalPort()
            }
        }
    }
}