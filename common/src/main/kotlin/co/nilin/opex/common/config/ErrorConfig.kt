package co.nilin.opex.common.config

import co.nilin.opex.common.utils.CustomErrorTranslator
import co.nilin.opex.utility.error.spi.ErrorTranslator
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.support.ReloadableResourceBundleMessageSource

@Configuration
class ErrorConfig {
    @Bean
    fun messageSource(): MessageSource {
        val messageSource = ReloadableResourceBundleMessageSource()
        messageSource.setBasename("classpath:messages")
        messageSource.setCacheSeconds(10) //reload messages every 10 seconds
        messageSource.setDefaultEncoding("UTF-8")
        return messageSource
    }

    @Bean
    @ConditionalOnMissingBean(ErrorTranslator::class)
    fun translator(): ErrorTranslator {
        return CustomErrorTranslator(messageSource = messageSource())
    }
}