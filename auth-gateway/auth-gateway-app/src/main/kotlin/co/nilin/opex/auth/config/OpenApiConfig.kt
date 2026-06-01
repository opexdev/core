package co.nilin.opex.auth.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class AuthGatewayOpenApiConfig(
    @Value("\${app.openapi.server-url:}")
    private val serverUrl: String
) {

    @Bean
    fun authGatewayOpenApi(): OpenAPI {
        val openApi = OpenAPI()
            .info(
                Info()
                    .title("Opex Auth Gateway API")
                    .description("OpenAPI documentation for Opex Auth Gateway APIs.")
                    .version("1.0.1-beta.7")
                    .description("Backend for opex exchange.")
            )
            .components(
                Components().addSecuritySchemes(
                    "bearerAuth",
                    SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT Bearer token")
                )
            )
        if (serverUrl.isNotBlank()) {
            openApi.servers(
                listOf(
                    Server()
                        .url(serverUrl)
                        .description("Public API server")
                )
            )
        }
        return openApi
    }
}
