package co.nilin.opex.captcha.app.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket

@Configuration
class SwaggerConfig {
    @Bean
    fun opex(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
            .groupName("opex-captcha")
            .apiInfo(apiInfo())
            .select()
            .paths(PathSelectors.regex("^/actuator.*").negate())
            .build()
            .useDefaultResponseMessages(false)
    }

    private fun apiInfo(): ApiInfo {
        return ApiInfoBuilder()
            .title("OPEX API")
            .description("Backend for opex exchange.")
            .license("MIT License")
            .licenseUrl("https://github.com/opexdev/Back-end/blob/feature/1-MVP/LICENSE")
            .version("0.1")
            .build()
    }
}
