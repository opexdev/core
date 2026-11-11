package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.ActiveUserStat
import co.nilin.opex.api.core.inout.DailyLoginStat
import co.nilin.opex.api.core.spi.DeviceManagementProxy
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
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/opex/v1/admin/stats")
@Tag(name = "Login Stats Admin", description = "Admin usage statistics.")
class LoginStatAdminController(
    private val deviceManagementProxy: DeviceManagementProxy
) {

    @GetMapping("/daily-logins", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Daily login counts",
        description = """GET /opex/v1/admin/stats/daily-logins.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Validation: `from` and `to` are epoch millis, both inclusive.
Behavior: One entry per day that had at least one login, oldest first. `userCount` counts people, not sessions and not devices: a user who logged in from a phone and a laptop on the same day counts once.
What a login means: a login creates a session row keyed by the identity provider's session state. A token refresh reuses that key and only moves the expiry, so a session is dated by the moment the user actually logged in and a refresh never counts as a new login.
Days with no login are absent from the response rather than present with a zero.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response. Empty when no login happened in the range.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = DailyLoginStat::class))
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required authority is missing: ROLE_admin. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun dailyLogins(
        @Parameter(name = "from", description = "Start of the range, epoch millis, inclusive.", required = true)
        @RequestParam("from") from: Long,
        @Parameter(name = "to", description = "End of the range, epoch millis, inclusive.", required = true)
        @RequestParam("to") to: Long,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): List<DailyLoginStat> {
        return deviceManagementProxy.getDailyLogins(
            securityContext.jwtAuthentication().tokenValue(),
            from,
            to
        )
    }

    @GetMapping("/active-users", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Active user count",
        description = """GET /opex/v1/admin/stats/active-users.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Validation: `from` and `to` are epoch millis, both inclusive. `to` must not be before `from`.
Behavior: One number, `userCount`, counting distinct people who used the system in the window. Unlike daily-logins this includes people who did not log in during the window but arrived with a session they already had, so it answers "how many people actually showed up".
How it is measured: the auth gateway emits a login event on token refresh as well as on login, and each event rewrites the user's last-activity timestamp. An active client refreshes within the access token lifespan, so a user browsing the site keeps that timestamp moving.
Accuracy: `exact` is true only while the window still reaches the present moment, which is the normal case of asking about today. The stored timestamp keeps only each user's latest activity, so for a window that has already closed the count is the number of people whose last activity ever fell inside it and anyone who returned afterwards is missing; `exact` is false there. For a historical series per day use daily-logins, which is backed by immutable rows.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ActiveUserStat::class)
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required authority is missing: ROLE_admin. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun activeUsers(
        @Parameter(name = "from", description = "Start of the window, epoch millis, inclusive.", required = true)
        @RequestParam("from") from: Long,
        @Parameter(name = "to", description = "End of the window, epoch millis, inclusive.", required = true)
        @RequestParam("to") to: Long,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): ActiveUserStat {
        return deviceManagementProxy.getActiveUsers(
            securityContext.jwtAuthentication().tokenValue(),
            from,
            to
        )
    }
}
