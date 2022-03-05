package co.nilin.opex.referral.app.config

import co.nilin.opex.referral.app.controller.*
import com.fasterxml.classmate.TypeResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.annotation.CurrentSecurityContext
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.OAuthBuilder
import springfox.documentation.builders.PathSelectors
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
    private lateinit var authUrl: String

    @Autowired
    private lateinit var typeResolver: TypeResolver

    @Bean
    fun opexBCGateway(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
            .groupName("opex-referral")
            .apiInfo(apiInfo())
            .select()
            .paths(PathSelectors.regex("^/actuator.*").negate())
            .build()
            .additionalModels(
                typeResolver.resolve(CheckoutController.CheckoutRecordBody::class.java),
                typeResolver.resolve(CommissionController.CommissionRewardBody::class.java),
                typeResolver.resolve(CodeController.ReferralCodeBody::class.java),
                typeResolver.resolve(ReferenceController.ReferenceBody::class.java),
                typeResolver.resolve(ReportController.ReferrerReportBody::class.java),
                typeResolver.resolve(ConfigController.ConfigsBody::class.java)
            )
            .ignoredParameterTypes(
                AuthenticationPrincipal::class.java,
                CurrentSecurityContext::class.java,
                Principal::class.java
            )
            .useDefaultResponseMessages(false)
            .securitySchemes(Collections.singletonList(oauth()))
            .securityContexts(Collections.singletonList(securityContext()))
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

    private fun oauth(): SecurityScheme {
        return OAuthBuilder()
            .name("opex")
            .grantTypes(grantTypes())
            .scopes(scopes())
            .build()
    }

    private fun scopes(): List<AuthorizationScope?> {
        return listOf(AuthorizationScope("openid", "OpenId"))
    }

    private fun grantTypes(): List<GrantType?> {
        val tokenUrl = "$authUrl/auth/realms/opex/protocol/openid-connect/token"
        val grantType = ResourceOwnerPasswordCredentialsGrant(tokenUrl)
        return Collections.singletonList(grantType)
    }

    private fun securityContext(): SecurityContext {
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
    fun securityInfo(): SecurityConfiguration {
        return SecurityConfigurationBuilder.builder()
            .clientId("admin-cli")
            .realm("opex")
            .appName("opex")
            .scopeSeparator(",")
            .build()
    }
}
