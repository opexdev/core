package co.nilin.opex.common.utils

import co.nilin.opex.utility.error.data.DefaultExceptionResponse
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.utility.error.spi.ErrorTranslator
import co.nilin.opex.utility.error.spi.ExceptionResponse
import org.slf4j.Logger
import org.springframework.context.MessageSource
import java.util.*


class CustomErrorTranslator(private val messageSource: MessageSource) : ErrorTranslator {
    private val logger: Logger by LoggerDelegate()
    override fun translate(ex: OpexException): ExceptionResponse {
        logger.info("hiiiiiiiiiiiiii")
        return DefaultExceptionResponse(
                ex.error.errorName(),
                ex.error.code(),
                messageSource.getMessage(ex.error.code().toString(), null, Locale("fa")),
                ex.status ?: ex.error.status(),
                ex.data,
                ex.crimeScene
        )
    }
}