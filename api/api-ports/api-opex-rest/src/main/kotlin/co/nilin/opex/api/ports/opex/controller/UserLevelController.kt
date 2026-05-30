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
    name = "Use Level",
    description = "Fetch user level config, public endpoint"
)
public class UserLevelController(private val configProxy: ConfigProxy) {
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
}
