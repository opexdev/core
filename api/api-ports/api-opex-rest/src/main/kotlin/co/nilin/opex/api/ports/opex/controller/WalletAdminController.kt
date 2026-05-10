package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.WalletDataResponse
import co.nilin.opex.api.core.inout.WalletTotal
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.ArraySchema
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

@RestController
@RequestMapping("/opex/v1/admin/wallet")
@Tag(name = "Wallet Admin", description = "Admin operations on wallets")
class WalletAdminController(
    private val walletProxy: WalletProxy
) {

    @GetMapping("/users")
    @Operation(
        summary = "Admin: List users wallets",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(name = "uuid", `in` = ParameterIn.QUERY, required = false, schema = Schema(type = "string")),
            Parameter(name = "currency", `in` = ParameterIn.QUERY, required = false, schema = Schema(type = "string")),
            Parameter(name = "excludeSystem", `in` = ParameterIn.QUERY, required = false, schema = Schema(type = "boolean", defaultValue = "false")),
            Parameter(name = "limit", `in` = ParameterIn.QUERY, required = false, schema = Schema(type = "integer", defaultValue = "10")),
            Parameter(name = "offset", `in` = ParameterIn.QUERY, required = false, schema = Schema(type = "integer", defaultValue = "0"))
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "OK", content = [
                Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = WalletDataResponse::class)))
            ])
        ]
    )
    suspend fun getUsersWallets(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestParam(required = false) uuid: String?,
        @RequestParam(required = false) currency: String?,
        @RequestParam(required = false, defaultValue = "false") excludeSystem: Boolean,
        @RequestParam limit: Int?,
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
        summary = "Admin: System wallets total",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "OK", content = [
                Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = WalletTotal::class)))
            ])
        ]
    )
    suspend fun getSystemWalletsTotal(@CurrentSecurityContext securityContext: SecurityContext): List<WalletTotal> {
        return walletProxy.getSystemWalletsTotal(securityContext.jwtAuthentication().tokenValue())
    }

    @GetMapping("/users/total")
    @Operation(
        summary = "Admin: Users wallets total",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "OK", content = [
                Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = WalletTotal::class)))
            ])
        ]
    )
    suspend fun getUsersWalletsTotal(@CurrentSecurityContext securityContext: SecurityContext): List<WalletTotal>? {
        return walletProxy.getUsersWalletsTotal(securityContext.jwtAuthentication().tokenValue())
    }
}
