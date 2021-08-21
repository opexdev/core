package co.nilin.opex.utility.error

import co.nilin.opex.utility.error.spi.ErrorTranslator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
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

    @Bean
    fun mapper(): ObjectMapper {
        return ObjectMapper().registerKotlinModule()
    }

}