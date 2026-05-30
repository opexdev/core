package co.nilin.opex.api.app.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig() {

    @Bean
    fun opexOpenApi(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Opex API")
                    .description("OpenAPI documentation for Opex REST APIs.")
                    .version("1.0.1-beta.7")
                    .description("Backend for opex exchange.")
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        "bearerAuth",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("JWT Bearer token")
                    )
            )
    }
}

