package co.nilin.opex.api.app.interceptor

import co.nilin.opex.api.app.service.APIKeyServiceImpl
import co.nilin.opex.api.core.spi.APIKeyFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

@Component
class APIKeyFilterImpl(private val apiKeyService: APIKeyServiceImpl) : APIKeyFilter, OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        val key = request.getHeader("X-API-KEY")
        if (!key.isNullOrEmpty()) {
            val secret = request.getHeader("X-API-SECRET")
            if (secret.isNullOrEmpty()) {
                chain.doFilter(request, response)
                return
            }

            val apiKey = apiKeyService.getAPIKey(key, secret)
            if (apiKey != null && apiKey.isEnabled && apiKey.accessToken != null && !apiKey.isExpired) {
                val auth = "Bearer ${apiKey.accessToken}"
                val wReq = object : HttpServletRequestWrapper(request) {
                    override fun getHeader(name: String?): String? {
                        return if (name?.equals("Authorization", true) == true)
                            auth
                        else
                            super.getHeader(name)
                    }

                    override fun getHeaders(name: String?): Enumeration<String> {
                        return if (name?.equals("Authorization", true) == true)
                            Collections.enumeration(listOf(auth))
                        else
                            super.getHeaders(name)
                    }

                    override fun getHeaderNames(): Enumeration<String> {
                        val names = mutableListOf<String>()
                        request.headerNames?.toList()?.let { h -> h.forEach { names.add(it) } }
                        names.add("Authorization")
                        return Collections.enumeration(names.distinct())
                    }
                }

                chain.doFilter(wReq, response)
                return
            }
        }
        chain.doFilter(request, response)
    }

}