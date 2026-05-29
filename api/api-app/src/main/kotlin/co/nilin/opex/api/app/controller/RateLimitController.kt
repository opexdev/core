package co.nilin.opex.api.app.controller

import co.nilin.opex.api.core.spi.RateLimitConfigService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/rate-limit")
@Tag(
    name = "API App - Rate Limit",
    description = "Rate-limit administration operations."
)
class RateLimitController(
    private val rateLimitConfig: RateLimitConfigService,
) {

    @GetMapping
    @Operation(
        summary = "Reload rate-limit config",
        description = """POST /v1/rate-limit.
Security: Bearer admin-token required. Required authority: ROLE_admin.

Behavior: Reloads rate-limit configuration from the configured source.
Response body: No response body.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response. No response body.", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun reloadRateLimits() {
        rateLimitConfig.loadConfig()
    }
}
