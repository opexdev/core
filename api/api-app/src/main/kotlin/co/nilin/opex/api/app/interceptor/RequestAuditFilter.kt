package co.nilin.opex.api.app.interceptor

import co.nilin.opex.common.security.JwtUtils
import com.fasterxml.jackson.databind.ObjectMapper
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequestDecorator
import org.springframework.http.server.reactive.ServerHttpResponseDecorator
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class RequestAuditFilter(
    private val objectMapper: ObjectMapper
) : WebFilter {

    private val logger = LoggerFactory.getLogger(RequestAuditFilter::class.java)
    private val maxPayloadSize = 10_000

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val request = exchange.request
        val sourceIp = resolveClientIp(exchange)
        val token = extractBearerToken(request.headers)
        val mobile = extractClaim(token, "mobile", "phone_number")
        val email = extractClaim(token, "email")
        val deviceUuid = extractClaim(token, "deviceUuid", "device_uuid")

        return DataBufferUtils.join(request.body)
            .defaultIfEmpty(exchange.response.bufferFactory().wrap(ByteArray(0)))
            .flatMap { requestBuffer ->
                val requestBytes = ByteArray(requestBuffer.readableByteCount())
                requestBuffer.read(requestBytes)
                DataBufferUtils.release(requestBuffer)
                val requestBody = truncateBody(String(requestBytes, StandardCharsets.UTF_8))

                val decoratedRequest = object : ServerHttpRequestDecorator(request) {
                    override fun getBody() = Flux.just(exchange.response.bufferFactory().wrap(requestBytes))
                }

                val responseBody = StringBuilder()
                val decoratedResponse = object : ServerHttpResponseDecorator(exchange.response) {
                    override fun writeWith(body: Publisher<out org.springframework.core.io.buffer.DataBuffer>): Mono<Void> {
                        val wrapped = Flux.from(body).map { dataBuffer ->
                            val bytes = ByteArray(dataBuffer.readableByteCount())
                            dataBuffer.read(bytes)
                            DataBufferUtils.release(dataBuffer)
                            responseBody.append(String(bytes, StandardCharsets.UTF_8))
                            bufferFactory().wrap(bytes)
                        }
                        return super.writeWith(wrapped)
                    }

                    override fun writeAndFlushWith(body: Publisher<out Publisher<out org.springframework.core.io.buffer.DataBuffer>>): Mono<Void> {
                        return writeWith(Flux.from(body).flatMapSequential { it })
                    }
                }

                val updatedExchange = exchange.mutate()
                    .request(decoratedRequest)
                    .response(decoratedResponse)
                    .build()

                return@flatMap chain.filter(updatedExchange)
                    .doFinally {
                        val payload = mapOf(
                            "date" to OffsetDateTime.now(ZoneOffset.UTC).toString(),
                            "ip" to sourceIp,
                            "mobile" to mobile,
                            "email" to email,
                            "deviceUuid" to deviceUuid,
                            "method" to request.method.name(),
                            "url" to request.uri.toString(),
                            "requestData" to requestBody,
                            "responseStatus" to (decoratedResponse.statusCode?.value() ?: updatedExchange.response.statusCode?.value()),
                            "responseBody" to truncateBody(responseBody.toString())
                        )
                        runCatching {
                            logger.info("API_REQUEST_AUDIT {}", objectMapper.writeValueAsString(payload))
                        }.onFailure {
                            logger.warn("Failed to write request audit log", it)
                        }
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

    private fun truncateBody(body: String): String {
        if (body.length <= maxPayloadSize) return body
        return body.take(maxPayloadSize) + "...(truncated)"
    }
}
