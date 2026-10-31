package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.FiatScannerResponse
import co.nilin.opex.api.core.inout.PodiumStatementsRequest
import co.nilin.opex.api.core.spi.FiatScannerProxy
import co.nilin.opex.api.ports.proxy.security.RequestSigner
import co.nilin.opex.common.utils.LoggerDelegate
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
class FiatScannerProxyImpl(
    @Qualifier("fiatScannerWebClient") private val client: WebClient,
    private val objectMapper: ObjectMapper,
    @Value("\${app.fiat-scanner.url}") private val baseUrl: String,
    @Value("\${app.fiat-scanner.secret-key}") private val secretKey: String
) : FiatScannerProxy {

    private val logger by LoggerDelegate()

    override suspend fun fetchPodiumStatements(request: PodiumStatementsRequest): FiatScannerResponse {
        val path = "/v1/admin/podium/statements"
        val body = objectMapper.writeValueAsBytes(request)

        return client.post()
            .uri(URI.create("$baseUrl$path"))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .headers { it.putAll(signatureHeaders("POST", path, "", body)) }
            .bodyValue(body)
            .exchangeToMono { response ->
                response.bodyToMono(String::class.java)
                    .defaultIfEmpty("")
                    .map { FiatScannerResponse(response.statusCode().value(), it) }
            }
            .awaitSingle()
            .also { logger.info("fiat-scanner podium statements answered ${it.status}") }
    }

    override suspend fun listWebhooks(status: String?, limit: Int?, offset: Int?): FiatScannerResponse {
        val path = "/v1/admin/webhooks"
        // Built once and used for both the signature and the URI: the far side hashes the raw
        // query string it receives, so the two have to be the same text
        val rawQuery = buildList {
            status?.takeIf { it.isNotBlank() }
                ?.let { add("status=" + URLEncoder.encode(it, StandardCharsets.UTF_8)) }
            limit?.let { add("limit=$it") }
            offset?.let { add("offset=$it") }
        }.joinToString("&")
        val uri = if (rawQuery.isEmpty()) "$baseUrl$path" else "$baseUrl$path?$rawQuery"

        return client.get()
            .uri(URI.create(uri))
            .accept(MediaType.APPLICATION_JSON)
            .headers { it.putAll(signatureHeaders("GET", path, rawQuery, ByteArray(0))) }
            .exchangeToMono { response ->
                response.bodyToMono(String::class.java)
                    .defaultIfEmpty("")
                    .map { FiatScannerResponse(response.statusCode().value(), it) }
            }
            .awaitSingle()
            .also { logger.info("fiat-scanner webhook list answered ${it.status}") }
    }

    override suspend fun retryWebhook(id: Long): FiatScannerResponse {
        val path = "/v1/admin/webhooks/$id/retry"

        return client.post()
            .uri(URI.create("$baseUrl$path"))
            .accept(MediaType.APPLICATION_JSON)
            .headers { it.putAll(signatureHeaders("POST", path, "", ByteArray(0))) }
            .exchangeToMono { response ->
                response.bodyToMono(String::class.java)
                    .defaultIfEmpty("")
                    .map { FiatScannerResponse(response.statusCode().value(), it) }
            }
            .awaitSingle()
            .also { logger.info("fiat-scanner webhook $id retry answered ${it.status}") }
    }

    private fun signatureHeaders(
        method: String,
        path: String,
        rawQuery: String,
        body: ByteArray
    ): Map<String, List<String>> {
        val timestamp = System.currentTimeMillis()
        val signature = RequestSigner.sign(
            secretKey = secretKey,
            timestamp = timestamp,
            method = method,
            path = path,
            rawQuery = rawQuery,
            body = body
        )
        return mapOf(
            "X-Timestamp" to listOf(timestamp.toString()),
            "X-Signature" to listOf(signature)
        )
    }
}
