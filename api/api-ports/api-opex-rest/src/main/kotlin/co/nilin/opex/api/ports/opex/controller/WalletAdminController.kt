package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.WalletDataResponse
import co.nilin.opex.api.core.inout.WalletTotal
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag

@RestController
@RequestMapping("/opex/v1/admin/wallet")
@Tag(name = "Wallet Admin", description = "Admin wallet overview and total balance operations.")
class WalletAdminController(
    private val walletProxy: WalletProxy
) {

    @GetMapping("/users")
    @Operation(
        summary = "Get users wallets",
        description = """GET /opex/v1/admin/wallet/users.
Behavior: Optional filters include uuid, currency, excludeSystem, limit, and offset. `excludeSystem` defaults to false.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Allowed values:
- excludeSystem: true, false.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = WalletDataResponse::class)))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun getUsersWallets(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "uuid", description = "User/profile/terminal UUID depending on the endpoint context.", required = false)
        @RequestParam(required = false) uuid: String?,
        @Parameter(name = "currency", description = "Currency symbol, e.g. USDT.", required = false)
        @RequestParam(required = false) currency: String?,
        @Parameter(name = "excludeSystem", description = "Whether system wallets should be excluded from the result.", required = false)
        @RequestParam(required = false, defaultValue = "false") excludeSystem: Boolean,
        @Parameter(name = "limit", description = "Optional page size.", required = false)
        @RequestParam limit: Int?,
        @Parameter(name = "offset", description = "Optional page offset.", required = false)
        @RequestParam offset: Int?
    ): List<WalletDataResponse> {
        return walletProxy.getUsersWallets(
            securityContext.jwtAuthentication().tokenValue(),
            uuid,
            currency,
            excludeSystem,
            limit ?: 10,
            offset ?: 0
        )
    }

    @GetMapping("/system/total")
    @Operation(
        summary = "Get system wallets total",
        description = """GET /opex/v1/admin/wallet/system/total.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Allowed values:
- excludeSystem: true, false.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = WalletTotal::class)))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun getSystemWalletsTotal(@Parameter(hidden = true)
                                      @CurrentSecurityContext securityContext: SecurityContext): List<WalletTotal> {
        return walletProxy.getSystemWalletsTotal(securityContext.jwtAuthentication().tokenValue())
    }

    @GetMapping("/users/total")
    @Operation(
        summary = "Get users wallets total",
        description = """GET /opex/v1/admin/wallet/users/total.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Allowed values:
- excludeSystem: true, false.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = WalletTotal::class)))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun getUsersWalletsTotal(@Parameter(hidden = true)
                                     @CurrentSecurityContext securityContext: SecurityContext): List<WalletTotal>? {
        return walletProxy.getUsersWalletsTotal(securityContext.jwtAuthentication().tokenValue())
    }
}
