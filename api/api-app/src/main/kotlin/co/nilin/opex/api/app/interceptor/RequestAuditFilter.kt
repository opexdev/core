package co.nilin.opex.api.app.interceptor

import co.nilin.opex.common.security.JwtUtils
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.time.OffsetDateTime
import java.time.ZoneOffset


@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class RequestAuditFilter(
    private val objectMapper: ObjectMapper
) : WebFilter {

    private val logger = LoggerFactory.getLogger(RequestAuditFilter::class.java)

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val request = exchange.request
        val sourceIp = resolveClientIp(exchange)
        val token = extractBearerToken(request.headers)
        val mobile = extractClaim(token, "mobile", "phone_number")
        val email = extractClaim(token, "email")
        val deviceUuid = extractClaim(token, "deviceUuid", "device_uuid")

        return chain.filter(exchange).doFinally {
            val payload = mapOf(
                "date" to OffsetDateTime.now(ZoneOffset.UTC).toString(),
                "ip" to sourceIp,
                "mobile" to mobile,
                "email" to email,
                "deviceUuid" to deviceUuid,
                "method" to request.method.name(),
                "url" to request.uri.toString(),
                "contentType" to request.headers.contentType?.toString(),
                "responseStatus" to exchange.response.statusCode?.value()
            )
            runCatching {
                logger.info("API_REQUEST_AUDIT {}", objectMapper.writeValueAsString(payload))
            }.onFailure {
                logger.warn("Failed to write request audit log", it)
            }
        }
    }

    private fun extractBearerToken(headers: HttpHeaders): String? {
        val header = headers.getFirst(HttpHeaders.AUTHORIZATION) ?: return null
        if (!header.startsWith("Bearer ", true)) return null
        return header.substringAfter("Bearer ").trim().takeIf { it.isNotBlank() }
    }

    private fun extractClaim(token: String?, vararg names: String): String? {
        if (token.isNullOrBlank()) return null
        val payload = runCatching { JwtUtils.decodePayload(token) }.getOrNull() ?: return null
        return names.firstNotNullOfOrNull { name ->
            payload[name]?.toString()?.takeIf { it.isNotBlank() }
        }
    }

    private fun resolveClientIp(exchange: ServerWebExchange): String? {
        val realIp = exchange.request.headers.getFirst("X-Real-IP")?.takeIf { it.isNotBlank() }
        if (realIp != null) {
            return realIp
        }
        val forwardedFor = exchange.request.headers.getFirst("X-Forwarded-For")
        if (!forwardedFor.isNullOrBlank()) {
            return forwardedFor.substringBefore(",").trim()
        }
        return exchange.request.remoteAddress?.address?.hostAddress
    }
}
