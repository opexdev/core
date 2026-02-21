package co.nilin.opex.wallet.ports.proxy.storage.impl

import co.nilin.opex.wallet.core.spi.StorageProxy
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import java.io.File

@Component
class StorageProxyImpl(private val webClient: WebClient) : StorageProxy {

    @Value("\${app.storage.url}")
    private lateinit var baseUrl: String

    override suspend fun systemUploadFile(
        bucket: String,
        key: String,
        file: File,
        isPublic: Boolean?
    ) {
        val bodyBuilder = MultipartBodyBuilder()

        bodyBuilder.part("file", FileSystemResource(file))
            .filename(file.name)
            .contentType(MediaType.APPLICATION_JSON)

        webClient.post()
            .uri("$baseUrl/v2/internal") {
                it.queryParam("bucket", bucket)
                it.queryParam("key", key)
                it.queryParam("isPublic", isPublic)
                it.build()
            }
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .retrieve()
            .onStatus({ it.isError }) { it.createException() }
            .awaitBodilessEntity()
    }

}
