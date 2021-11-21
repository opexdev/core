package co.nilin.opex.utility.error.spi

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.http.HttpStatus

abstract class ExceptionResponse(@JsonIgnore val status: HttpStatus)