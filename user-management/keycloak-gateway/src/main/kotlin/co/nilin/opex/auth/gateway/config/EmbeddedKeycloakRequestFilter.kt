package co.nilin.opex.auth.gateway.config

import org.keycloak.common.ClientConnection
import org.keycloak.services.filters.AbstractRequestFilter
import java.io.UnsupportedEncodingException
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest


class EmbeddedKeycloakRequestFilter : AbstractRequestFilter(), Filter {
    @Throws(UnsupportedEncodingException::class)
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse?, filterChain: FilterChain) {
        servletRequest.characterEncoding = "UTF-8"
        val clientConnection = createConnection(servletRequest as HttpServletRequest)
        filter(clientConnection) {
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
                return request.remoteAddr
            }

            override fun getRemoteHost(): String {
                return request.remoteHost
            }

            override fun getRemotePort(): Int {
                return request.remotePort
            }

            override fun getLocalAddr(): String {
                return request.localAddr
            }

            override fun getLocalPort(): Int {
                return request.localPort
            }
        }
    }
}
