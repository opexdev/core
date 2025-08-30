package co.nilin.opex.common.config

import co.nilin.opex.common.translation.CustomErrorTranslator
import co.nilin.opex.utility.error.spi.ErrorTranslator
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
class ErrorConfig {


    @Bean
    @Primary
    fun translator(): ErrorTranslator {
        return CustomErrorTranslator()
    }

}