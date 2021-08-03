package co.nilin.mixchange.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors.regex
import springfox.documentation.service.ApiInfo
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket

@SpringBootApplication
@ComponentScan("co.nilin.mixchange")
class MatchingGatewayApp {
    @Bean
    fun opexMatchingGateway(): Docket? {
        return Docket(DocumentationType.SWAGGER_2)
            .groupName("opex-matching-gateway")
            .apiInfo(apiInfo())
            .select()
            .paths(regex("^/actuator.*").negate())
            .build()
    }

    private fun apiInfo(): ApiInfo? {
        return ApiInfoBuilder()
            .title("OPEX API")
            .description("Backend for opex exchange.")
            .license("MIT License")
            .licenseUrl("https://github.com/opexdev/Back-end/blob/feature/1-MVP/LICENSE")
            .version("0.1")
            .build()
    }
}

fun main(args: Array<String>) {
    runApplication<MatchingGatewayApp>(*args)
}