package co.nilin.opex.kyc.ports.poxy.imp

import co.nilin.opex.kyc.core.spi.StorageProxy
import co.nilin.opex.kyc.core.data.UploadResult
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.net.URI

inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}
@Component
class StorageProxyImp(@Qualifier("loadBalanced") private val webClient: WebClient) : StorageProxy {
    @Value("\${app.storage.url}")
    private lateinit var baseUrl: String

    override suspend fun uploadFile(file: FilePart, name: String, reference: String) :UploadResult {
       return webClient.post()
                .uri(URI.create("$baseUrl/${reference}"))
                .header("Content-Type", "application/json")
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono(typeRef<UploadResult>())
                .log()
                .awaitFirst()
    }
}