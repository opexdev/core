package co.nilin.opex.api.app.interceptor

import co.nilin.opex.api.app.service.APIKeyServiceImpl
import co.nilin.opex.api.core.spi.APIKeyFilter
import kotlinx.coroutines.reactor.mono
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
        val secret = request.headers["X-API-SECRET"]

        if (key.isNullOrEmpty() || secret.isNullOrEmpty()) {
            return chain.filter(exchange)
        }
        return mono {
            val apiKey = apiKeyService.getAPIKey(key[0], secret[0])
            if (apiKey != null && apiKey.isEnabled && apiKey.accessToken != null && !apiKey.isExpired) {
                val req = exchange.request.mutate()
                        .header("Authorization", "Bearer ${apiKey.accessToken}")
                        .build()
                exchange.mutate().request(req).build()
            } else null
        }.flatMap { updatedExchange ->
            if (updatedExchange != null)
                chain.filter(updatedExchange)
            else
                chain.filter(exchange)
        }
    }
}