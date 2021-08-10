package co.nilin.mixchange.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.security.core.annotation.AuthenticationPrincipal
import springfox.documentation.builders.*
import springfox.documentation.builders.PathSelectors.regex
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
@EnableSwagger2
@ComponentScan("co.nilin.mixchange")
class ApiApp {
    @Bean
    fun opexApi(): Docket? {
        return Docket(DocumentationType.SWAGGER_2)
            .groupName("opex-api")
            .apiInfo(apiInfo())
            .select()
            .paths(regex("^/api/v3.*"))
            .build()
            .globalRequestParameters(
                singletonList(
                    RequestParameterBuilder()
                        .name("content-type")
                        .description("content-type")
                        .`in`(ParameterType.HEADER)
                        .required(true)
                        .build()
                )
            )
            .ignoredParameterTypes(AuthenticationPrincipal::class.java, Principal::class.java)
            .useDefaultResponseMessages(false)
            .securitySchemes(singletonList(oauth()))
            .securityContexts(singletonList(securityContext()))
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

    @Bean
    fun oauth(): SecurityScheme? {
        return OAuthBuilder()
            .name("opex")
            .grantTypes(grantTypes())
            .scopes(scopes())
            .build()
    }

    fun scopes(): List<AuthorizationScope?>? {
        return emptyList()
    }

    fun grantTypes(): List<GrantType?>? {
        val tokenUrl = "http://localhost:8083/auth/realms/mixchange/protocol/openid-connect/token"
        val grantType = ResourceOwnerPasswordCredentialsGrant(tokenUrl)
        return singletonList(grantType)
    }

    @Bean
    fun securityContext(): SecurityContext? {
        val securityReference = SecurityReference.builder()
            .reference("opex")
            .scopes(emptyArray())
            .build()
        return SecurityContext.builder()
            .securityReferences(singletonList(securityReference))
            .operationSelector { true }
            .build()
    }

    @Bean
    fun securityInfo(): SecurityConfiguration? {
        return SecurityConfigurationBuilder.builder()
            .clientId("admin-cli")
            .realm("mixchange")
            .appName("opex")
            .scopeSeparator(",")
            .build()
    }
}

fun main(args: Array<String>) {
    runApplication<ApiApp>(*args)
}
