package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.spi.StorageProxy
import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import reactor.core.publisher.Mono

@Component
class StorageProxyImpl(@Qualifier("generalWebClient") private val webClient: WebClient) : StorageProxy {

    private val logger by LoggerDelegate()

    @Value("\${app.storage.url}")
    private lateinit var baseUrl: String

    override suspend fun adminDownload(
        token: String,
        bucket: String,
        key: String
    ): ResponseEntity<ByteArray> {
        return webClient.get()
            .uri("$baseUrl/v2/admin") {
                it.queryParam("bucket", bucket)
                it.queryParam("key", key)
                it.build()
            }
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .accept(
                MediaType.APPLICATION_OCTET_STREAM,
                MediaType.APPLICATION_JSON
            )
            .exchangeToMono { response ->
                if (response.statusCode().isError) {
                    response.createException().flatMap { Mono.error(it) }
                } else {
                    response.toEntity(ByteArray::class.java)
                }
            }
            .awaitSingle()
    }

    override suspend fun adminUpload(
        token: String,
        bucket: String,
        key: String,
        file: FilePart,
        isPublic : Boolean?
    ) {
        webClient.post()
            .uri("$baseUrl/v2/admin"){
                it.queryParam("isPublic", isPublic)
                it.queryParam("bucket", bucket)
                it.queryParam("key", key)
                it.build()
            }
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(
                BodyInserters.fromMultipartData(
                    LinkedMultiValueMap<String, Any>().apply {
                        add("file", file)
                    }
                ))
            .retrieve()
            .onStatus({ it.isError }) { it.createException() }
            .awaitBodilessEntity()
    }

    override suspend fun adminDelete(
        token: String,
        bucket: String,
        key: String
    ) {
        webClient.delete()
            .uri("$baseUrl/v2/admin") {
                it.queryParam("bucket", bucket)
                it.queryParam("key", key)
                it.build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }) { it.createException() }
            .awaitBodilessEntity()
    }
}
