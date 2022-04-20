package co.nilin.opex.utility.error

import co.nilin.opex.utility.error.spi.ErrorTranslator
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Config {

    @Bean
    @ConditionalOnMissingBean
    fun translator(): ErrorTranslator {
        return DefaultErrorTranslator()
    }

}