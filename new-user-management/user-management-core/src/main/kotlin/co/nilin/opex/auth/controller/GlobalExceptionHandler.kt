package co.nilin.opex.auth.controller

import co.nilin.opex.auth.exception.UserAlreadyExistsException
import co.nilin.opex.auth.exception.UserNotFoundException
import co.nilin.opex.auth.model.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.server.ServerWebExchange
import java.time.Instant

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun handleUserAlreadyExists(ex: UserAlreadyExistsException
    , exchange: ServerWebExchange
    ): ResponseEntity<ErrorResponse> {
        val path = exchange.request.path.value()
        return ResponseEntity(ErrorResponse(Instant.now(), HttpStatus.CONFLICT.value(), UserAlreadyExistsException::class.simpleName!!, ex.message!!, path), HttpStatus.CONFLICT)
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserAlreadyExists(ex: UserNotFoundException
                                , exchange: ServerWebExchange
    ): ResponseEntity<ErrorResponse> {
        val path = exchange.request.path.value()
        return ResponseEntity(ErrorResponse(Instant.now(), HttpStatus.NOT_FOUND.value(), UserNotFoundException::class.simpleName!!, ex.message!!, path), HttpStatus.CONFLICT)
    }
}