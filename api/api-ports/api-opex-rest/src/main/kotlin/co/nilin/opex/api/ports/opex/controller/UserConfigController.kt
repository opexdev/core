package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.UpdateUserConfigRequest
import co.nilin.opex.api.core.inout.UserWebConfig
import co.nilin.opex.api.core.spi.ConfigProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import co.nilin.opex.common.data.WebConfig
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
    name = "Config",
    description = "Web and user configuration operations."
)
class UserConfigController(private val configProxy: ConfigProxy) {

    @GetMapping("/web/config")
    @Operation(
        summary = "Get web config",
        description = """GET /opex/v1/web/config.
Security: Public endpoint. No Bearer token is required.

Allowed values:
- language: EN, FA, AR, UZ.
- calender: JALALI, HIJRI, GREGORIAN.
- theme: DARK, LIGHT, SYSTEM.""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = WebConfig::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized. No response body.", content = [Content()])
        ]
    )
    suspend fun getWebConfig(): WebConfig {
        return configProxy.getWebConfig()
    }

    @GetMapping("/user/config")
    @Operation(
        summary = "Get user config",
        description = """GET /opex/v1/user/config.
Security: Bearer user-token required. Requires authenticated user JWT.

Allowed values:
- language: EN, FA, AR, UZ.
- calender: JALALI, HIJRI, GREGORIAN.
- theme: DARK, LIGHT, SYSTEM.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = UserWebConfig::class)
                )]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized. No response body.", content = [Content()])
        ]
    )
    suspend fun getUserConfig(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): UserWebConfig {
        return configProxy.getUserConfig(securityContext.jwtAuthentication().tokenValue())
    }

    @PutMapping("/user/config")
    @Operation(
        summary = "Update user config",
        description = """PUT /opex/v1/user/config.
Security: Bearer user-token required. Requires authenticated user JWT.

Allowed values:
- language: EN, FA, AR, UZ.
- calender: JALALI, HIJRI, GREGORIAN.
- theme: DARK, LIGHT, SYSTEM.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = UpdateUserConfigRequest::class)
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = UserWebConfig::class)
                )]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized. No response body.", content = [Content()])
        ]
    )
    suspend fun updateConfig(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: UpdateUserConfigRequest
    ): UserWebConfig {
        return configProxy.updateUserConfig(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @GetMapping("/user/config/pair")
    @Operation(
        summary = "Get favorite pairs",
        description = """GET /opex/v1/user/config/pair.
Security: Bearer user-token required. Requires authenticated user JWT.
""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(type = "string"))
                )]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized. No response body.", content = [Content()])
        ]
    )
    suspend fun getUserFavoritePair(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): Set<String> {
        return configProxy.getUserFavoritePair(securityContext.jwtAuthentication().tokenValue())
    }

    @PostMapping("/user/config/pair/{pair}")
    @Operation(
        summary = "Add favorite pair",
        description = """POST /opex/v1/user/config/pair/{pair}.
Security: Bearer user-token required. Requires authenticated user JWT.
""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(type = "string"))
                )]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized. No response body.", content = [Content()])
        ]
    )
    suspend fun addUserFavoritePair(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "pair", description = "Market pair symbol.", required = true)
        @PathVariable pair: String
    ): Set<String> {
        return configProxy.addUserFavoritePair(securityContext.jwtAuthentication().tokenValue(), pair)
    }

    @DeleteMapping("/user/config/pair/{pair}")
    @Operation(
        summary = "Remove favorite pair",
        description = """DELETE /opex/v1/user/config/pair/{pair}.
Security: Bearer user-token required. Requires authenticated user JWT.
""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(type = "string"))
                )]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized. No response body.", content = [Content()])
        ]
    )
    suspend fun removeUserFavoritePair(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "pair", description = "Market pair symbol.", required = true)
        @PathVariable pair: String
    ): Set<String> {
        return configProxy.removeUserFavoritePair(securityContext.jwtAuthentication().tokenValue(), pair)
    }
}
