package co.nilin.opex.common.translation

import co.nilin.opex.utility.error.data.DefaultExceptionResponse
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.utility.error.spi.ErrorTranslator
import co.nilin.opex.utility.error.spi.ExceptionResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class CustomErrorTranslator() : ErrorTranslator {
    @Autowired
    private lateinit var translationCacheService: TranslationCacheService

    private val logger = LoggerFactory.getLogger(CustomErrorTranslator::class.java)

    override fun translate(ex: OpexException): Mono<ExceptionResponse> {
        return getUserLanguage().map {
            logger.info("going to translate error based on user language:{}", it)

            DefaultExceptionResponse(
                ex.error.errorName(),
                ex.error.code(),
                translationCacheService.getMessage(ex.error.errorName().toString(), it),
                ex.status ?: ex.error.status(),
                ex.data,
                ex.crimeScene
            )
        }
    }

    fun getUserLanguage(): Mono<String> =
        Mono.deferContextual { ctx -> Mono.just(ctx.getOrDefault("lang", "EN")!!) }
}




