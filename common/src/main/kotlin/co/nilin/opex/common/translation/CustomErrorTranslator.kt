package co.nilin.opex.common.translation

import co.nilin.opex.utility.error.data.DefaultExceptionResponse
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.utility.error.data.UserLanguage
import co.nilin.opex.utility.error.spi.ErrorTranslator
import co.nilin.opex.utility.error.spi.ExceptionResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class CustomErrorTranslator() : ErrorTranslator {
    @Autowired
    private lateinit var translationCacheService: TranslationCacheService

    private val logger = LoggerFactory.getLogger(CustomErrorTranslator::class.java)
    override fun translate(ex: OpexException, userLanguage: UserLanguage?): ExceptionResponse {
        logger.info("going to translate messages (custom) and user language is {}",userLanguage?.name)
        return DefaultExceptionResponse(
            ex.error.errorName(),
            ex.error.code(),
            translationCacheService.getMessage(ex.error.errorName().toString(), userLanguage?.name),
            ex.status ?: ex.error.status(),
            ex.data,
            ex.crimeScene
        )
    }


}


