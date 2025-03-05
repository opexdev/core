package co.nilin.opex.market.app.config

import co.nilin.opex.common.OpexError
import co.nilin.opex.market.app.service.RateLimiterService
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class RateLimitInterceptor(
    private val rateLimiterService: RateLimiterService,
) : WebFilter {

    private val privatePaths: Set<String> = setOf(
        "/v1/chart/v1/hi",
        "/v1/chart/v1/hi2",
        "/v1/chart/v1/hi3"
    )
    private val globalPaths: Set<String> = setOf(
        "/v1/chart/v2/hi",
        "/v1/chart/v2/hi2",
        "/v1/chart/v2/hi3"
    )

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val apiPath = exchange.request.uri.path

        return when (apiPath) {
            in privatePaths -> handlePrivatePath(exchange, chain, apiPath)
            in globalPaths -> handleGlobalPath(exchange, chain, apiPath)
            else -> chain.filter(exchange)
        }
    }

    private fun handlePrivatePath(exchange: ServerWebExchange, chain: WebFilterChain, apiPath: String): Mono<Void> {
        return ReactiveSecurityContextHolder.getContext()
            .switchIfEmpty(Mono.error(OpexError.UnAuthorized.exception()))
            .flatMap { context ->
                val auth = context.authentication ?: return@flatMap Mono.error(OpexError.UnAuthorized.exception())
                if (!auth.isAuthenticated) return@flatMap Mono.error(OpexError.UnAuthorized.exception())

                val userId = auth.name
                val allowed = rateLimiterService.checkRateLimit(userId, 2, 60, apiPath)
                if (allowed) chain.filter(exchange)
                else Mono.error(OpexError.TooManyRequests.exception("Rate limit exceeded for user $userId"))
            }
    }

    private fun handleGlobalPath(exchange: ServerWebExchange, chain: WebFilterChain, apiPath: String): Mono<Void> {
        val allowed = rateLimiterService.checkRateLimit("global", 10, 60, apiPath)
        return if (allowed) chain.filter(exchange)
        else Mono.error(OpexError.TooManyRequests.exception("Global rate limit exceeded"))
    }
}