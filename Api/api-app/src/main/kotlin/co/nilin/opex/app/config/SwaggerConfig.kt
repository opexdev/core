package co.nilin.opex.app.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.annotation.AuthenticationPrincipal
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.OAuthBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestParameterBuilder
import springfox.documentation.service.*
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger.web.SecurityConfiguration
import springfox.documentation.swagger.web.SecurityConfigurationBuilder
import java.security.Principal
import java.util.*

@Configuration
class SwaggerConfig {
    @Value("\${swagger.authUrl}")
    val authUrl: String = ""

    @Bean
    fun opexApi(): Docket? {
        return Docket(DocumentationType.SWAGGER_2)
            .groupName("opex-api")
            .apiInfo(apiInfo())
            .select()
            .paths(PathSelectors.regex("^/api/v3.*"))
            .build()
            .globalRequestParameters(
                Collections.singletonList(
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
            .securitySchemes(Collections.singletonList(oauth()))
            .securityContexts(Collections.singletonList(securityContext()))
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

    private fun oauth(): SecurityScheme? {
        return OAuthBuilder()
            .name("opex")
            .grantTypes(grantTypes())
            .scopes(scopes())
            .build()
    }

    private fun scopes(): List<AuthorizationScope?>? {
        return listOf(AuthorizationScope("openid", "OpenId"))
    }

    private fun grantTypes(): List<GrantType?>? {
        val tokenUrl = "$authUrl/auth/realms/opex/protocol/openid-connect/token"
        val grantType = ResourceOwnerPasswordCredentialsGrant(tokenUrl)
        return Collections.singletonList(grantType)
    }

    private fun securityContext(): SecurityContext? {
        val securityReference = SecurityReference.builder()
            .reference("opex")
            .scopes(emptyArray())
            .build()
        return SecurityContext.builder()
            .securityReferences(Collections.singletonList(securityReference))
            .operationSelector { true }
            .build()
    }

    @Bean
    fun securityInfo(): SecurityConfiguration? {
        return SecurityConfigurationBuilder.builder()
            .clientId("admin-cli")
            .realm("opex")
            .appName("opex")
            .scopeSeparator(",")
            .build()
    }
}