package co.nilin.opex.api.ports.opex.controller;

import co.nilin.opex.api.core.inout.UserLevelConfig
import co.nilin.opex.api.core.spi.ConfigProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
@RestController
@RequestMapping("/opex/v1")
@Tag(
    name = "Admin User Config",
    description = "User level configuration operations."
)
public class UserLevelAdminController(private val configProxy: ConfigProxy) {
    @GetMapping("/user-level/config")
    @Operation(
        summary = "Get user level config",
        description = """GET /opex/v1/user-level/config.
Security: Public endpoint. No Bearer token is required.""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = UserLevelConfig::class))
                )]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized. No response body.", content = [Content()])
        ]
    )
    suspend fun getUserLevelConfig(): List<UserLevelConfig> {
        return configProxy.getUserLevelConfig()
    }

    @PutMapping("/admin/user-level/config")
    @Operation(
        summary = "Update user level config",
        description = """PUT /opex/v1/admin/user-level/config.
Security: Bearer admin-token required. Required authority: ROLE_admin.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = UserLevelConfig::class)
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = UserLevelConfig::class)
                )]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. No response body.", content = [Content()])
        ]
    )
    suspend fun updateUserLevelConfig(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody userLevelConfig: UserLevelConfig
    ): UserLevelConfig {
        return configProxy.updateUserLevelConfig(securityContext.jwtAuthentication().tokenValue(), userLevelConfig)
    }

    @DeleteMapping("/admin/user-level/config/{userLevel}/{language}")
    @Operation(
        summary = "Delete user level config",
        description = """DELETE /opex/v1/admin/user-level/config/{userLevel}/{language}.
Security: Bearer admin-token required. Required authority: ROLE_admin.

Allowed values:
- language: EN, FA, AR, UZ.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response. No response body.",
                content = [Content()]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. No response body.", content = [Content()])
        ]
    )
    suspend fun deleteUserLevelConfig(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "userLevel", description = "User level key.", required = true)
        @PathVariable userLevel: String,
        @Parameter(name = "language", description = "Language: EN, FA, AR, UZ.", required = true)
        @PathVariable language: String
    ) {
        configProxy.deleteUserLevelConfig(securityContext.jwtAuthentication().tokenValue(), userLevel, language)
    }
}
