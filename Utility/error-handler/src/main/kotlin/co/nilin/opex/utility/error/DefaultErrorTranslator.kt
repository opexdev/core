package co.nilin.opex.utility.error

import co.nilin.opex.utility.error.data.DefaultExceptionResponse
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.utility.error.spi.ErrorTranslator
import co.nilin.opex.utility.error.spi.ExceptionResponse
import org.springframework.stereotype.Component

@Component
class DefaultErrorTranslator : ErrorTranslator {

    override fun translate(ex: OpexException): ExceptionResponse {
        return DefaultExceptionResponse(
            ex.error.name,
            ex.error.code,
            ex.message ?: ex.error.message,
            ex.status ?: ex.error.status,
            ex.data,
            ex.crimeScene
        )
    }
}