package co.nilin.opex.wallet.app

import co.nilin.opex.utility.error.EnableOpexErrorHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.security.core.annotation.AuthenticationPrincipal
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.OAuthBuilder
import springfox.documentation.builders.PathSelectors.regex
import springfox.documentation.builders.RequestParameterBuilder
import springfox.documentation.service.*
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger.web.SecurityConfiguration
import springfox.documentation.swagger.web.SecurityConfigurationBuilder
import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.security.Principal
import java.util.Collections.singletonList

@SpringBootApplication
@ComponentScan("co.nilin.opex")
@EnableSwagger2
@EnableOpexErrorHandler
class WalletApp

fun main(args: Array<String>) {
    runApplication<WalletApp>(*args)
}
