package co.nilin.opex.api.app.interceptor

import co.nilin.opex.api.app.service.APIKeyServiceImpl
import co.nilin.opex.api.core.spi.APIKeyFilter
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class APIKeyFilterImpl(private val apiKeyService: APIKeyServiceImpl) : APIKeyFilter, WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val request = exchange.request
        val key = request.headers["X-API-KEY"]
        if (!key.isNullOrEmpty()) {
            val secret = request.headers["X-API-SECRET"]
            if (secret.isNullOrEmpty())
                return chain.filter(exchange)

            val apiKey = runBlocking { apiKeyService.getAPIKey(key[0], secret[0]) }
            if (apiKey != null && apiKey.isEnabled && apiKey.accessToken != null && !apiKey.isExpired) {
                val req = exchange.request.mutate()
                    .header("Authorization", "Bearer ${apiKey.accessToken}")
                    .build()
                return chain.filter(exchange.mutate().request(req).build())
            }
        }
        return chain.filter(exchange)
    }

}