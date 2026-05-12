package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.CurrencyGatewayCommand
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag

@RestController
@RequestMapping("/opex/v1/admin")
@Tag(name = "Gateway Admin", description = "Admin gateway management for currencies.")
class GatewayAdminController(
    private val walletProxy: WalletProxy
) {
    @PostMapping("/{currencySymbol}/gateway")
    @Operation(
        summary = "Add currency to gateway",
        description = """POST /opex/v1/admin/{currencySymbol}/gateway.
Security: Bearer admin-token required. Required authority: ROLE_admin.

Behavior: Gateway fields differ by gateway type. Keep off-chain transfer-method fields and on-chain/token fields consistent with the gateway being created or updated.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Response body: See schema.", content = [Content(mediaType = "application/json", schema = Schema(implementation = CurrencyGatewayCommand::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun addCurrencyToGateway(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("currencySymbol") currencySymbol: String,
        @RequestBody body: CurrencyGatewayCommand
    ): CurrencyGatewayCommand? {
        return walletProxy.addCurrencyToGateway(securityContext.jwtAuthentication().tokenValue(), currencySymbol, body)
    }


    @PutMapping("/{currencySymbol}/gateway/{uuid}")
    @Operation(
        summary = "Update gateway",
        description = """PUT /opex/v1/admin/{currencySymbol}/gateway/{uuid}.
Security: Bearer admin-token required. Required authority: ROLE_admin.

Behavior: Gateway fields differ by gateway type. Keep off-chain transfer-method fields and on-chain/token fields consistent with the gateway being created or updated.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Response body: See schema.", content = [Content(mediaType = "application/json", schema = Schema(implementation = CurrencyGatewayCommand::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun updateGateway(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("uuid") gatewayUuid: String,
        @PathVariable("currencySymbol") currencySymbol: String,
        @RequestBody body: CurrencyGatewayCommand
    ): CurrencyGatewayCommand? {
        return walletProxy.updateGateway(
            securityContext.jwtAuthentication().tokenValue(),
            gatewayUuid,
            currencySymbol,
            body
        )
    }

    @GetMapping("/{currencySymbol}/gateway/{uuid}")
    @Operation(
        summary = "Get gateway",
        description = """GET /opex/v1/admin/{currencySymbol}/gateway/{uuid}.
Security: Bearer admin-token required. Required authority: ROLE_admin.

Behavior: Gateway fields differ by gateway type. Keep off-chain transfer-method fields and on-chain/token fields consistent with the gateway being created or updated.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Response body: See schema.", content = [Content(mediaType = "application/json", schema = Schema(implementation = CurrencyGatewayCommand::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun getGateway(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("uuid") gatewayUuid: String,
        @PathVariable("currencySymbol") currencySymbol: String
    ): CurrencyGatewayCommand? {
        return walletProxy.getGateway(
            securityContext.jwtAuthentication().tokenValue(),
            gatewayUuid,
            currencySymbol
        )
    }

    @DeleteMapping("/{currencySymbol}/gateway/{uuid}")
    @Operation(
        summary = "Delete gateway",
        description = """DELETE /opex/v1/admin/{currencySymbol}/gateway/{uuid}.
Security: Bearer admin-token required. Required authority: ROLE_admin.

Behavior: Gateway fields differ by gateway type. Keep off-chain transfer-method fields and on-chain/token fields consistent with the gateway being created or updated.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "No response body.", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun deleteGateway(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("uuid") gatewayUuid: String,
        @PathVariable("currencySymbol") currencySymbol: String
    ) {
        walletProxy.deleteGateway(securityContext.jwtAuthentication().tokenValue(), gatewayUuid, currencySymbol)
    }

}
