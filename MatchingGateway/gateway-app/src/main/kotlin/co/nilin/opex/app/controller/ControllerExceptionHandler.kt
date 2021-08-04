package co.nilin.opex.app.controller

import co.nilin.opex.app.exception.NotAllowedToSubmitOrderException
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.nio.charset.StandardCharsets
import java.util.*

@RestControllerAdvice
class ControllerExceptionHandler {

    data class ErrorResponse(
        val timestamp: Date, val status: Int, val error: String, val message: String
    )

    val logger = LoggerFactory.getLogger(ControllerExceptionHandler::class.java)

    val objectMapper: ObjectMapper = ObjectMapper()

    @ExceptionHandler(NotAllowedToSubmitOrderException::class)
    fun handle(ex: NotAllowedToSubmitOrderException): ResponseEntity<ErrorResponse> {
        logger.error("Trace Error {}", ex)
        val ret = ResponseEntity.status(500).body(
            ErrorResponse(
                Date(), -1, ex::class.qualifiedName ?: "", ex.message ?: ""
            )
        )
        logger.debug("return error response:{}", ret)
        return ret
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class WebClientErrorResponse {
        constructor() {

        }

        constructor(timestamp: Date?, path: String?, status: Int?, error: String?, message: String?) {
            this.timestamp = timestamp
            this.path = path
            this.status = status
            this.error = error
            this.message = message
        }

        var timestamp: Date? = null
        var path: String? = null
        var status: Int? = null
        var error: String? = null
        var message: String? = null
    }

    @ExceptionHandler(WebClientResponseException::class)
    fun handle(ex: WebClientResponseException): ResponseEntity<ErrorResponse> {
        logger.error("Trace Error {}", ex)
        try {
            val body = objectMapper.readValue(
                ex.responseBodyAsByteArray.toString(StandardCharsets.UTF_8),
                WebClientErrorResponse::class.java
            )
            val ret = ResponseEntity.status(body.status ?: ex.rawStatusCode).body(
                ErrorResponse(
                    Date(),
                    body.status ?: ex.rawStatusCode,
                    body.error ?: ex::class.qualifiedName ?: "",
                    body.message ?: "Internal Server Error"
                )
            )
            logger.debug("return error response:{}", ret)
            return ret
        } catch (je: Exception) {
            logger.error("Trace Error {}", je)
            val ret = ResponseEntity.status(ex.statusCode).body(
                ErrorResponse(
                    Date(), ex.rawStatusCode, ex::class.qualifiedName ?: "", "Internal Server Error"
                )
            )
            logger.debug("return error response:{}", ret)
            return ret
        }
    }

    @ExceptionHandler(Throwable::class)
    fun handle(ex: Throwable): ResponseEntity<ErrorResponse> {
        logger.error("Trace Error {}", ex)
        val ret = ResponseEntity.status(500).body(
            ErrorResponse(
                Date(), 500, ex::class.qualifiedName ?: "", "Internal Server Error"
            )
        )
        logger.debug("return error response:{}", ret)
        return ret
    }
}
