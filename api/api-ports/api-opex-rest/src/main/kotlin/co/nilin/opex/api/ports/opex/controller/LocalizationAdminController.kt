package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.BlockchainGatewayProxy
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import co.nilin.opex.common.OpexError
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
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/opex/v1/admin")
@Tag(name = "Localization Admin", description = "Admin localization management for currencies, terminals, and gateways")
class LocalizationAdminController(
    private val walletProxy: WalletProxy,
    private val blockchainGatewayProxy: BlockchainGatewayProxy
) {
    @GetMapping("/currency/{currency}/localization")
    @Operation(
        summary = "Admin: get currency localizations",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(name = "currency", `in` = ParameterIn.PATH, required = true, schema = Schema(type = "string"))
        ],
        responses = [ ApiResponse(responseCode = "200", description = "OK", content = [ Content(mediaType = "application/json", schema = Schema(implementation = CurrencyLocalizationResponse::class)) ]) ]
    )
    suspend fun getCurrencyLocalizations(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("currency") currency: String
    ): CurrencyLocalizationResponse {
        return walletProxy.getCurrencyLocalizations(securityContext.jwtAuthentication().tokenValue(), currency)
    }

    @PostMapping("/currency/{currency}/localization")
    @Operation(
        summary = "Admin: save currency localizations",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [ Parameter(name = "currency", `in` = ParameterIn.PATH, required = true, schema = Schema(type = "string")) ],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = [ Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = CurrencyLocalizationCommand::class))) ]),
        responses = [ ApiResponse(responseCode = "200", description = "OK", content = [ Content(mediaType = "application/json", schema = Schema(implementation = CurrencyLocalizationResponse::class)) ]) ]
    )
    suspend fun saveCurrencyLocalizations(
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
        summary = "Admin: delete currency localization",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [ Parameter(name = "id", `in` = ParameterIn.PATH, required = true, schema = Schema(type = "integer", format = "int64")) ],
        responses = [ ApiResponse(responseCode = "200", description = "Deleted") ]
    )
    suspend fun deleteCurrencyLocalization(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("id") id: Long
    ) {
        walletProxy.deleteCurrencyLocalization(securityContext.jwtAuthentication().tokenValue(), id)
    }

    @GetMapping("/terminal/{terminalUuid}/localization")
    @Operation(
        summary = "Admin: get terminal localizations",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [ Parameter(name = "terminalUuid", `in` = ParameterIn.PATH, required = true, schema = Schema(type = "string")) ],
        responses = [ ApiResponse(responseCode = "200", description = "OK", content = [ Content(mediaType = "application/json", schema = Schema(implementation = TerminalLocalizationResponse::class)) ]) ]
    )
    suspend fun getTerminalLocalizations(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("terminalUuid") terminalUuid: String
    ): TerminalLocalizationResponse {
        return walletProxy.getTerminalLocalizations(securityContext.jwtAuthentication().tokenValue(), terminalUuid)
    }

    @PostMapping("/terminal/{terminalUuid}/localization")
    @Operation(
        summary = "Admin: save terminal localizations",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [ Parameter(name = "terminalUuid", `in` = ParameterIn.PATH, required = true, schema = Schema(type = "string")) ],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = [ Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = TerminalLocalizationCommand::class))) ]),
        responses = [ ApiResponse(responseCode = "200", description = "OK", content = [ Content(mediaType = "application/json", schema = Schema(implementation = TerminalLocalizationResponse::class)) ]) ]
    )
    suspend fun saveTerminalLocalizations(
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
        summary = "Admin: delete terminal localization",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [ Parameter(name = "id", `in` = ParameterIn.PATH, required = true, schema = Schema(type = "integer", format = "int64")) ],
        responses = [ ApiResponse(responseCode = "200", description = "Deleted") ]
    )
    suspend fun deleteTerminalLocalization(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("id") id: Long
    ) {
        walletProxy.deleteTerminalLocalization(securityContext.jwtAuthentication().tokenValue(), id)
    }

    @GetMapping("/gateway/{gatewayUuid}/localization")
    @Operation(
        summary = "Admin: get gateway localization",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [ Parameter(name = "gatewayUuid", `in` = ParameterIn.PATH, required = true, schema = Schema(type = "string")) ],
        responses = [ ApiResponse(responseCode = "200", description = "OK", content = [ Content(mediaType = "application/json", schema = Schema(implementation = GatewayLocalizationResponse::class)) ]) ]
    )
    suspend fun getGatewayLocalization(
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
        summary = "Admin: save gateway localizations",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [ Parameter(name = "gatewayUuid", `in` = ParameterIn.PATH, required = true, schema = Schema(type = "string")) ],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = [ Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = GatewayLocalizationCommand::class))) ]),
        responses = [ ApiResponse(responseCode = "200", description = "OK", content = [ Content(mediaType = "application/json", schema = Schema(implementation = GatewayLocalizationResponse::class)) ]) ]
    )
    suspend fun saveGatewayLocalizations(
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
        summary = "Admin: delete gateway localization",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(name = "gatewayUuid", `in` = ParameterIn.PATH, required = true, schema = Schema(type = "string")),
            Parameter(name = "id", `in` = ParameterIn.PATH, required = true, schema = Schema(type = "integer", format = "int64"))
        ],
        responses = [ ApiResponse(responseCode = "200", description = "Deleted") ]
    )
    suspend fun deleteGatewayLocalization(
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