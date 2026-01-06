package co.nilin.opex.api.app.service

import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.utility.error.spi.ErrorTranslator
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
@Order(-2)
class OpexFilterExceptionHandler(
    private val translator: ErrorTranslator,
    private val objectMapper: ObjectMapper
) : ErrorWebExceptionHandler {

    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {

        if (ex is OpexException) {
            return translator.translate(ex).flatMap { error ->
                exchange.response.statusCode = HttpStatusCode.valueOf(error.status.value())
                exchange.response.headers.contentType = MediaType.APPLICATION_JSON

                val bytes = objectMapper.writeValueAsBytes(error)
                val buffer = exchange.response.bufferFactory().wrap(bytes)

                exchange.response.writeWith(Mono.just(buffer))
            }
        }
        return Mono.error(ex)
    }
}