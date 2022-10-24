package co.nilin.opex.api.app.interceptor

import co.nilin.opex.api.app.service.APIKeyService
import kotlinx.coroutines.runBlocking
import org.springframework.web.filter.GenericFilterBean
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

class APIKeyFilter(private val apiKeyService: APIKeyService) : GenericFilterBean() {

    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        val request = servletRequest as HttpServletRequest
        val apiKey = request.getHeader("X-API-KEY")
        if (!apiKey.isNullOrEmpty()) {
            val secret = request.getHeader("X-API-SECRET")
            if (secret.isNullOrEmpty())
                filterChain.doFilter(servletRequest, servletResponse)

            val accessToken = runBlocking { apiKeyService.getAccessToken(apiKey, secret) }

        }
    }


}