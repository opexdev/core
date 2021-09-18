package co.nilin.opex.utility.error.controller

import co.nilin.opex.utility.error.data.DefaultExceptionResponse
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.utility.error.spi.ErrorTranslator
import co.nilin.opex.utility.error.spi.ExceptionResponse
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ServerWebInputException
import java.nio.charset.StandardCharsets
import java.util.*
import org.springframework.http.HttpStatus

@RestControllerAdvice
class ExceptionController(
    private val mapper: ObjectMapper,
    private val translator: ErrorTranslator
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class WebClientErrorResponse(
        val timestamp: Date?,
        val path: String?,
        val status: Int?,
        val error: String?,
        val message: String?,
        val code: Int?
    )

    private val logger = LoggerFactory.getLogger(ExceptionController::class.java)

    @ExceptionHandler(OpexException::class)
    fun handle(e: OpexException): ResponseEntity<ExceptionResponse> {
        val error = translator.translate(e)
        if (error is DefaultExceptionResponse)
            logger.error("Opex error happened at ${e.crimeScene?.name}", e)
        else
            logger.error("Opex error", e)
        return response(error)
    }

    @ExceptionHandler(WebClientResponseException::class)
    fun handle(e: WebClientResponseException): ResponseEntity<ExceptionResponse> {
        logger.error("Webclient error", e)
        return try {
            val body = mapper.readValue(
                e.responseBodyAsByteArray.toString(StandardCharsets.UTF_8),
                WebClientErrorResponse::class.java
            )

            val opexError = OpexError.findByCode(body.code)
            val er = translator.translate(OpexException(opexError ?: OpexError.InternalServerError))
            response(er)
        } catch (ex: Exception) {
            val opEx = OpexException(OpexError.InternalServerError)
            val er = translator.translate(opEx)
            response(er)
        }
    }

    @ExceptionHandler(ServerWebInputException::class)
    fun handleMissingServletRequestParameter(ex: ServerWebInputException): ResponseEntity<ExceptionResponse> {
        logger.error("Web input error", ex)
        val name = ex.methodParameter?.parameterName

        val error = OpexError.InvalidRequestParam
        val er = translator.translate(
            OpexException(
                error,
                String.format(error.message!!, name)
            )
        )
        return response(er)
    }

    @ExceptionHandler(Throwable::class)
    fun handle(e: Throwable): ResponseEntity<ExceptionResponse> {
        logger.error("Generic error", e)
        val opexException = OpexException(status = HttpStatus.INTERNAL_SERVER_ERROR, error = OpexError.InternalServerError)
        val error = translator.translate(opexException)
        return response(error)
    }

    private fun response(er: ExceptionResponse): ResponseEntity<ExceptionResponse> =
        ResponseEntity.status(er.status).body(er)

}