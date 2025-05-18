package co.nilin.opex.auth.proxy

import co.nilin.opex.auth.model.CaptchaType
import co.nilin.opex.common.OpexError
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class CaptchaProxy(
    @Value("\${captcha.enabled}") private val captchaEnabled: Boolean,
    @Qualifier("captchaWebClient") private val webClient: WebClient,
) {

    suspend fun validateCaptcha(proof: String, type: CaptchaType) {
        if (captchaEnabled) {
            val statusCode = webClient.get().uri("/verify") {
                it.queryParam("type", type)
                it.queryParam("proof", proof)
                it.build()
            }.accept(MediaType.APPLICATION_JSON).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchangeToMono { response -> Mono.just(response.statusCode()) }.awaitFirstOrNull()

            when (statusCode) {
                HttpStatus.NO_CONTENT -> return
                HttpStatus.BAD_REQUEST -> throw OpexError.InvalidCaptcha.exception()
                else -> throw OpexError.BadRequest.exception("Error in verify captcha")
            }
        }
    }
}