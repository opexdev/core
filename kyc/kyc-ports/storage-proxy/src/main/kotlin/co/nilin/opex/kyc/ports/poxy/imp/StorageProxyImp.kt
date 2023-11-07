package co.nilin.opex.kyc.ports.poxy.imp

import co.nilin.opex.kyc.core.data.UploadResult
import co.nilin.opex.kyc.core.spi.StorageProxy
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.net.URI

inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

@Component
class StorageProxyImp(@Qualifier("loadBalanced") private val webClient: WebClient) : StorageProxy {
    @Value("\${app.storage.url}")
    private lateinit var baseUrl: String
    private val logger = LoggerFactory.getLogger(StorageProxyImp::class.java)

    override suspend fun uploadFile(file: FilePart, name: String, reference: String): UploadResult {
        val builder = MultipartBodyBuilder()
        builder.part("file", file)
        return webClient.post()
                .uri(URI.create("$baseUrl/${reference}"))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .onStatus({ t -> t.isError }, { throw OpexException(OpexError.UnableToUploadFiles) })
                .bodyToMono(typeRef<UploadResult>())
                .log()
                .awaitFirst()
    }
}