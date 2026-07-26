package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.UserFee
import co.nilin.opex.api.core.spi.AccountantProxy
import co.nilin.opex.common.utils.Interval
import co.nilin.opex.common.utils.LimitedInterval
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
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
import java.math.BigDecimal

@RestController
@RequestMapping("/opex/v1/user/data")
@Tag(
    name = "User Exchange Data",
    description = "Authenticated user fee, volume, and activity data."
)
class UserDataController(
    private val accountantProxy: AccountantProxy
) {

    @GetMapping("/trade/volume")
    @Operation(
        summary = "Get trade volume by currency",
        description = """GET /opex/v1/user/data/trade/volume.
Validation: `interval` must be one of Day, Week, Month, Year.
Security: Bearer user-token required. Requires authenticated user JWT.
Allowed values:
- interval: Day, Week, Month, Year.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(type = "number"))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getTradeVolumeByCurrency(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(
            name = "symbol",
            description = "Trading pair or currency symbol, depending on endpoint.",
            required = true
        )
        @RequestParam symbol: String,
        @Parameter(
            name = "interval",
            description = "Interval. For user data endpoints allowed values are Day, Week, Month, Year. ",
            required = true
        )
        @RequestParam interval: LimitedInterval
    ): BigDecimal {
        val interval = Interval.valueOf(interval.name)
        val uuid = securityContext.authentication.name
        return accountantProxy.getTradeVolumeByCurrency(uuid, symbol, interval)
    }

    @GetMapping("/trade/volume/total")
    @Operation(
        summary = "Get total trade volume value",
        description = """GET /opex/v1/user/data/trade/volume/total.
Security: Bearer user-token required. Requires authenticated user JWT.
Allowed values:
- interval: Day, Week, Month, Year.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(type = "number"))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getTotalTradeVolumeValue(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(
            name = "interval",
            description = "Interval. For user data endpoints allowed values are Day, Week, Month, Year.",
            required = true
        )
        @RequestParam interval: LimitedInterval
    ): BigDecimal {
        val interval = Interval.valueOf(interval.name)
        val uuid = securityContext.authentication.name
        return accountantProxy.getTotalTradeVolumeValue(uuid, interval)
    }

    @GetMapping("/fee")
    @Operation(
        summary = "Get user fee",
        description = """GET /opex/v1/user/data/fee.
Security: Bearer user-token required. Requires authenticated user JWT.
""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = UserFee::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getUserFee(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): UserFee {
        return accountantProxy.getUserFee(securityContext.authentication.name)
    }

    @GetMapping("/withdraw/volume/total")
    @Operation(
        summary = "Get total withdraw volume value",
        description = """GET /opex/v1/user/data/withdraw/volume/total.
Behavior: `interval` is optional. Accepted labels map to Day, Week, Month, Year where supported.
Security: Bearer user-token required. Requires authenticated user JWT.
Allowed values:
- interval: Day, Week, Month, Year.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "No response body.", content = [Content()]),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getTotalWithdrawVolumeValue(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(
            name = "interval",
            description = "Interval. For user data endpoints allowed values are Day, Week, Month, Year. ",
            required = false
        )
        @RequestParam(required = false) interval: LimitedInterval?
    ): BigDecimal =
        accountantProxy.getTotalWithdrawVolumeValue(
            securityContext.authentication.name,
            interval?.let { Interval.valueOf(interval.name) }
        )

}
