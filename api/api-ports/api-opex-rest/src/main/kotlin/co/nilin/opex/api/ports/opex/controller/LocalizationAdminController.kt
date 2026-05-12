package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.BlockchainGatewayProxy
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import co.nilin.opex.common.OpexError
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
@Tag(name = "Localization Admin", description = "Admin localization management for currencies, terminals, and gateways.")
class LocalizationAdminController(
    private val walletProxy: WalletProxy,
    private val blockchainGatewayProxy: BlockchainGatewayProxy
) {
    @GetMapping("/currency/{currency}/localization")
    @Operation(
        summary = "Get currency localizations",
        description = """GET /opex/v1/admin/currency/{currency}/localization.
Security: Bearer admin-token required. Required authority: ROLE_admin.

Behavior: Send one localization object per language. Existing items can include `id`; new items should use null or omit `id`.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = CurrencyLocalizationResponse::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun getCurrencyLocalizations(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("currency") currency: String
    ): CurrencyLocalizationResponse {
        return walletProxy.getCurrencyLocalizations(securityContext.jwtAuthentication().tokenValue(), currency)
    }

    @PostMapping("/currency/{currency}/localization")
    @Operation(
        summary = "Save currency localizations",
        description = """POST /opex/v1/admin/currency/{currency}/localization.
Security: Bearer admin-token required. Required authority: ROLE_admin.

Behavior: Send one localization object per language. Existing items can include `id`; new items should use null or omit `id`.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = CurrencyLocalizationResponse::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun saveCurrencyLocalizations(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("currency") currency: String,
        @RequestBody currencyLocalizations: List<CurrencyLocalizationCommand>
    ): CurrencyLocalizationResponse {
        return walletProxy.saveCurrencyLocalizations(
            securityContext.jwtAuthentication().tokenValue(),
            currency,
            currencyLocalizations
        )
    }

    @DeleteMapping("/currency/localization/{id}")
    @Operation(
        summary = "Delete currency localization",
        description = """DELETE /opex/v1/admin/currency/localization/{id}.
Security: Bearer admin-token required. Required authority: ROLE_admin.

Behavior: Send one localization object per language. Existing items can include `id`; new items should use null or omit `id`.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "No response body.", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun deleteCurrencyLocalization(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("id") id: Long
    ) {
        walletProxy.deleteCurrencyLocalization(securityContext.jwtAuthentication().tokenValue(), id)
    }

    @GetMapping("/terminal/{terminalUuid}/localization")
    @Operation(
        summary = "Get terminal localizations",
        description = """GET /opex/v1/admin/terminal/{terminalUuid}/localization.
Security: Bearer admin-token required. Required authority: ROLE_admin.

Behavior: Send one localization object per language. Existing items can include `id`; new items should use null or omit `id`.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = TerminalLocalizationResponse::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun getTerminalLocalizations(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("terminalUuid") terminalUuid: String
    ): TerminalLocalizationResponse {
        return walletProxy.getTerminalLocalizations(securityContext.jwtAuthentication().tokenValue(), terminalUuid)
    }

    @PostMapping("/terminal/{terminalUuid}/localization")
    @Operation(
        summary = "Save terminal localizations",
        description = """POST /opex/v1/admin/terminal/{terminalUuid}/localization.
Security: Bearer admin-token required. Required authority: ROLE_admin.

Behavior: Send one localization object per language. Existing items can include `id`; new items should use null or omit `id`.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = TerminalLocalizationResponse::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun saveTerminalLocalizations(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("terminalUuid") terminalUuid: String,
        @RequestBody terminalLocalizations: List<TerminalLocalizationCommand>
    ): TerminalLocalizationResponse {
        return walletProxy.saveTerminalLocalizations(
            securityContext.jwtAuthentication().tokenValue(),
            terminalUuid,
            terminalLocalizations
        )
    }

    @DeleteMapping("/terminal/localization/{id}")
    @Operation(
        summary = "Delete terminal localization",
        description = """DELETE /opex/v1/admin/terminal/localization/{id}.
Security: Bearer admin-token required. Required authority: ROLE_admin.

Behavior: Send one localization object per language. Existing items can include `id`; new items should use null or omit `id`.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "No response body.", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun deleteTerminalLocalization(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("id") id: Long
    ) {
        walletProxy.deleteTerminalLocalization(securityContext.jwtAuthentication().tokenValue(), id)
    }

    @GetMapping("/gateway/{gatewayUuid}/localization")
    @Operation(
        summary = "Get gateway localization",
        description = """GET /opex/v1/admin/gateway/{gatewayUuid}/localization.
Security: Bearer admin-token required. Required authority: ROLE_admin.

Behavior: Send one localization object per language. Existing items can include `id`; new items should use null or omit `id`.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = GatewayLocalizationResponse::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun getGatewayLocalization(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("gatewayUuid") gatewayUuid: String
    ): GatewayLocalizationResponse {
        return if (gatewayUuid.startsWith("ofg")) {
            walletProxy.getOffChainGatewayLocalizations(
                securityContext.jwtAuthentication().tokenValue(),
                gatewayUuid
            )
        } else if (gatewayUuid.startsWith("ong")) {
            blockchainGatewayProxy.getOnChainGatewayLocalizations(
                securityContext.jwtAuthentication().tokenValue(), gatewayUuid
            )
        } else throw OpexError.GatewayNotFount.exception()
    }

    @PostMapping("/gateway/{gatewayUuid}/localization")
    @Operation(
        summary = "Save gateway localizations",
        description = """POST /opex/v1/admin/gateway/{gatewayUuid}/localization.
Security: Bearer admin-token required. Required authority: ROLE_admin.

Behavior: Send one localization object per language. Existing items can include `id`; new items should use null or omit `id`.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = GatewayLocalizationResponse::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun saveGatewayLocalizations(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("gatewayUuid") gatewayUuid: String,
        @RequestBody gatewayLocalizations: List<GatewayLocalizationCommand>
    ): GatewayLocalizationResponse {
        return if (gatewayUuid.startsWith("ofg")) {
            walletProxy.saveOffChainGatewayLocalizations(
                securityContext.jwtAuthentication().tokenValue(),
                gatewayUuid,
                gatewayLocalizations
            )
        } else if (gatewayUuid.startsWith("ong")) {
            blockchainGatewayProxy.saveOnChainGatewayLocalizations(
                securityContext.jwtAuthentication().tokenValue(),
                gatewayUuid,
                gatewayLocalizations
            )
        } else throw OpexError.GatewayNotFount.exception()
    }

    @DeleteMapping("/gateway/{gatewayUuid}/localization/{id}")
    @Operation(
        summary = "Delete gateway localization",
        description = """DELETE /opex/v1/admin/gateway/{gatewayUuid}/localization/{id}.
Security: Bearer admin-token required. Required authority: ROLE_admin.

Behavior: Send one localization object per language. Existing items can include `id`; new items should use null or omit `id`.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "No response body.", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun deleteGatewayLocalization(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("id") id: Long,
        @PathVariable("gatewayUuid") gatewayUuid: String
    ) {
        return if (gatewayUuid.startsWith("ofg")) {
            walletProxy.deleteOffChainGatewayLocalization(
                securityContext.jwtAuthentication().tokenValue(),
                id
            )
        } else if (gatewayUuid.startsWith("ong")) {
            blockchainGatewayProxy.deleteOnChainGatewayLocalization(
                securityContext.jwtAuthentication().tokenValue(),
                id
            )
        } else throw OpexError.GatewayNotFount.exception()
    }
}
