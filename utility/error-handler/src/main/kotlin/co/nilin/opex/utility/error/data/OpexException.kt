package co.nilin.opex.utility.error.data

import org.springframework.http.HttpStatus

class OpexException(
    val error: OpexError,
    message: String? = null,
    val status: HttpStatus? = null,
    val data: Any? = null,
    val crimeScene: Class<*>? = null
) : RuntimeException(message ?: error.message)