package co.nilin.opex.auth.proxy

import co.nilin.opex.auth.model.CaptchaType
import co.nilin.opex.common.OpexError
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class CaptchaProxy(@Qualifier("captchaWebClient") private val webClient: WebClient) {

    fun validateCaptcha(proof: String, type: CaptchaType) {
        webClient.get().uri("/verify") {
            it.queryParam("type", type)
            it.queryParam("proof", proof)
            it.build()
        }.accept(MediaType.APPLICATION_JSON).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchangeToMono { response ->
                when (response.statusCode()) {
                    HttpStatus.NO_CONTENT -> Mono.just(true)
                    HttpStatus.BAD_REQUEST -> Mono.error(OpexError.InvalidCaptcha.exception())
                    else -> Mono.error(OpexError.BadRequest.exception("Error in verify captcha"))
                }
            }
    }
}