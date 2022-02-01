package co.nilin.opex.auth.gateway.config

import co.nilin.opex.utility.error.DefaultErrorTranslator
import co.nilin.opex.utility.error.EnableOpexErrorHandler
import co.nilin.opex.utility.error.spi.ErrorTranslator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableOpexErrorHandler
class ErrorHandlerConfig {

    @Bean
    fun errorTranslator(): ErrorTranslator {
        return DefaultErrorTranslator()
    }

}