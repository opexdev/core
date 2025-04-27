package co.nilin.opex.common.utils

import co.nilin.opex.utility.error.data.DefaultExceptionResponse
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.utility.error.spi.ErrorTranslator
import co.nilin.opex.utility.error.spi.ExceptionResponse
import org.springframework.context.MessageSource
import java.util.*


class CustomErrorTranslator(private val messageSource: MessageSource) : ErrorTranslator {
    override fun translate(ex: OpexException): ExceptionResponse {
        return DefaultExceptionResponse(
            ex.error.errorName(),
            ex.error.code(),
            messageSource.getMessage(ex.error.errorName().toString(), null, "", Locale("fa")),
            ex.status ?: ex.error.status(),
            ex.data,
            ex.crimeScene
        )
    }
}