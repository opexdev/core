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
            val secret = request.getHeader("X-API-KEY")
            if (secret.isNullOrEmpty()) {
                chain.doFilter(request, response)
                return
            }

            val apiKey = apiKeyService.getAPIKey(key, secret)
            if (apiKey != null && apiKey.isEnabled && apiKey.accessToken != null && !apiKey.isExpired) {
                val wrappedReq = RequestWrapper(request)
                wrappedReq.addHeader("Authorization", "Bearer ${apiKey.accessToken}")
                chain.doFilter(wrappedReq, response)
                return
            }
        }
        chain.doFilter(request, response)
    }

}

class RequestWrapper(request: HttpServletRequest) : HttpServletRequestWrapper(request) {

    private val customHeaders = hashMapOf<String, String>()

    fun addHeader(key: String, value: String) {
        customHeaders[key] = value
    }

    override fun getHeaderNames(): Enumeration<String> {
        val names = HashSet(Collections.list(super.getHeaderNames()))
        names.addAll(customHeaders.keys)
        return Collections.enumeration(names)
    }
}