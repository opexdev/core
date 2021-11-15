package co.nilin.opex.wallet.app.config

import co.nilin.opex.utility.interceptor.FormDataWorkaroundFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.server.WebFilter

@Configuration
class RestConfig {
    @Bean
    fun formDataWebFilter(): WebFilter {
        return FormDataWorkaroundFilter()
    }
}