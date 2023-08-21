package co.nilin.opex.profile.ports.kyc.imp

import co.nilin.opex.kyc.core.data.ManualUpdateRequest
import co.nilin.opex.kyc.core.data.UploadResult
import co.nilin.opex.profile.core.spi.KycProxy
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserter
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.net.URI

inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

@Component
class KycProxyImp(@Qualifier("loadBalanced") private val webClient: WebClient) : KycProxy {
    @Value("\${app.kyc.url}")
    private lateinit var baseUrl: String
    private val logger = LoggerFactory.getLogger(KycProxyImp::class.java)


    override suspend fun updateKycLevel(updateKycLevelRequest: ManualUpdateRequest) {
         webClient.put()
                .uri(URI.create("$baseUrl/${updateKycLevelRequest.userId}"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateKycLevelRequest))
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono<HttpStatus>()
                .awaitFirst()
    }
}