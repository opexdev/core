package co.nilin.opex.common.config

import co.nilin.opex.common.service.CustomErrorTranslator
import co.nilin.opex.utility.error.spi.ErrorTranslator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class ErrorConfig {
    @Bean
    @Primary
    fun translator(): ErrorTranslator {
        return CustomErrorTranslator()
    }

}