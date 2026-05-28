package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.UserDetailAssetsSnapshot
import co.nilin.opex.api.core.inout.analytics.ActivityTotals
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.opex.service.UserActivityAggregationService
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/opex/v1/analytics")
@Tag(
    name = "User Analytics",
    description = "User analytics and user-asset analytics operations."
)
class UserAnalyticsController(
    private val userActivityAggregationService: UserActivityAggregationService,
    val walletProxy: WalletProxy
) {

    @GetMapping("/user-activity")
    @Operation(
        summary = "User activity",
        description = """GET /opex/v1/analytics/user-activity.
Security: Bearer user-token required. Requires authenticated user JWT.

Behavior:
- Returns activity totals for the last 31 days.
- Response object keys are epoch timestamps in milliseconds.
- All date/time values exposed by the API layer are timestamps.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response. Map key is epoch timestamp in milliseconds. Map value is daily activity totals.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(type = "object", additionalPropertiesSchema = ActivityTotals::class),
                    examples = [ExampleObject(
                        name = "User activity response",
                        value = """
{
  "1715731200000": {
    "totalBalance": 1000.50,
    "totalWithdraw": 20.00,
    "totalDeposit": 200.00,
    "totalTrade": 150.00,
    "totalOrder": 3
  }
}
                        """
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun userActivity(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): Map<Long, ActivityTotals> {
        val auth = securityContext.jwtAuthentication()
        return userActivityAggregationService.getLast31DaysUserStats(auth.tokenValue(), auth.name)
    }

    @GetMapping("/users-detail-assets")
    @Operation(
        summary = "Get user details assets",
        description = """GET /opex/v1/analytics/users-detail-assets.
Security: Public endpoint. No Bearer token is required.

Behavior:
- limit defaults to 10 when omitted.
- offset defaults to 0 when omitted.

""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = UserDetailAssetsSnapshot::class))
                )]
            )
        ]
    )
    suspend fun getUserDetailsAssets(
        @Parameter(name = "limit", description = "Optional page size.", required = false)
        @RequestParam limit: Int?,
        @Parameter(name = "offset", description = "Optional page offset.", required = false)
        @RequestParam offset: Int?
    ): List<UserDetailAssetsSnapshot> {
        return walletProxy.getUsersDetailAssets(limit ?: 10, offset ?: 0)
    }
}
