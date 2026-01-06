package co.nilin.opex.api.app.config

import co.nilin.opex.api.app.service.RateLimitCoordinatorService
import co.nilin.opex.api.core.spi.RateLimitConfigService
import co.nilin.opex.common.OpexError
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import org.springframework.web.util.pattern.PathPatternParser
import reactor.core.publisher.Mono

@Component
class RateLimitConfig(
    private val rateLimitConfig: RateLimitConfigService,
    private val coordinator: RateLimitCoordinatorService

) : WebFilter {
    private val logger = LoggerFactory.getLogger(RateLimitConfig::class.java)
    private val parser = PathPatternParser()

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {

        val endpoint = rateLimitConfig.getEndpoints()
            .asSequence()
            .filter { it.enabled }
            .filter { it.method.equals(exchange.request.method.name(), true) }
            .sortedByDescending { it.priority }
            .firstOrNull { endpoint ->
                val pattern = parser.parse(endpoint.url)
                pattern.matches(exchange.request.path)
            }

        if (endpoint == null) {
            return chain.filter(exchange)
        }

        return applyRateLimitIfAuthenticated(
            exchange,
            chain,
            endpoint.groupId
        )
    }


    private fun applyRateLimitIfAuthenticated(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
        groupId: Long
    ): Mono<Void> {

        return ReactiveSecurityContextHolder.getContext()
            .mapNotNull { it.authentication }
            .filter { it.isAuthenticated }
            .flatMap { auth ->
                if (auth != null && !auth.name.isNullOrBlank())
                    applyRateLimit(
                        auth.name, exchange, chain, groupId
                    )
                else
                    chain.filter(exchange)
            }

    }


    private fun applyRateLimit(
        identity: String,
        exchange: ServerWebExchange,
        chain: WebFilterChain,
        groupId: Long
    ): Mono<Void> {

        val group = rateLimitConfig.getGroup(groupId)
            ?: return chain.filter(exchange)

        val result = coordinator.check(
            identity = identity,
            groupId = groupId,
            maxRequests = group.requestCount,
            windowSeconds = group.requestWindowSeconds,
            apiPath = exchange.request.uri.path,
            apiMethod = exchange.request.method.name()
        )

        return if (result.blocked) {
            tooManyRequests(
                exchange,
                identity,
                exchange.request.uri.path,
                exchange.request.method.name(),
                result.retryAfterSeconds
            )
        } else {
            chain.filter(exchange)
        }
    }

    //TODO should throw opex error
    private fun tooManyRequests(
        exchange: ServerWebExchange,
        identity: String,
        url: String,
        method: String,
        retryAfterSeconds: Int
    ): Mono<Void> {
        logger.info("Rate limit exceeded ($identity) -- $method:$url")
        exchange.response.statusCode = HttpStatus.TOO_MANY_REQUESTS
        return exchange.response.writeWith(
            Mono.just(
                exchange.response.bufferFactory()
                    .wrap("Rate limit exceeded ($identity) -- $method:$url --  Retry-After, $retryAfterSeconds".toByteArray())
            )
        )
    }
}