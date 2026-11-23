package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.AppConfig
import co.nilin.opex.api.core.inout.UpdateWebConfigRequest
import co.nilin.opex.api.core.spi.ConfigProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import co.nilin.opex.common.data.WebConfig
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/opex/v1")
@Tag(
    name = "Admin Config",
    description = "Default web and user configuration operations."
)
class ConfigAdminController(private val configProxy: ConfigProxy) {


    @PutMapping("/admin/app/config")
    @Operation(
        summary = "Update app configuration",
        description = """PUT /opex/v1/admin/app/config.
Security: Bearer admin-token required. Required authority: ROLE_admin.

Updates the application configuration including API methods and account types.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(mediaType = "application/json", schema = Schema(implementation = AppConfig::class))]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = AppConfig::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. No response body.", content = [Content()])
        ]
    )
    suspend fun updateAppConfig(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody config: AppConfig
    ): AppConfig {
        return configProxy.updateAppConfig(securityContext.jwtAuthentication().tokenValue(), config)
    }

    @PutMapping("/admin/web/config")
    @Operation(
        summary = "Update web config",
        description = """PUT /opex/v1/admin/web/config.
Security: Bearer admin-token required. Required authority: ROLE_admin.

Allowed values:
- defaultLanguage/supportedLanguages: EN, FA, AR, UZ.
- supportedCalenders: JALALI, HIJRI, GREGORIAN.
- defaultTheme/supportedThemes: DARK, LIGHT, SYSTEM.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = UpdateWebConfigRequest::class)
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = WebConfig::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. No response body.", content = [Content()])
        ]
    )
    suspend fun updateWebConfig(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: UpdateWebConfigRequest
    ): WebConfig {
        return configProxy.updateWebConfig(securityContext.jwtAuthentication().tokenValue(), request)
    }

}
