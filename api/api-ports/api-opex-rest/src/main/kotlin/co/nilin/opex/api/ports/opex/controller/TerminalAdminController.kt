package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.CurrencyGatewayCommand
import co.nilin.opex.api.core.inout.TerminalCommand
import co.nilin.opex.api.core.inout.TerminalUpdateCommand
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
@RequestMapping("/opex/v1/admin/terminal")
@Tag(name = "Terminal Admin", description = "Admin terminal and terminal-gateway assignment operations.")
class TerminalAdminController(
    private val walletProxy: WalletProxy
) {
    @PostMapping
    @Operation(
        summary = "Register terminal",
        description = """POST /opex/v1/admin/terminal.
Security: Bearer admin-token required. Required authority: ROLE_admin.
""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = TerminalCommand::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun registerTerminal(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext, @RequestBody body: TerminalCommand
    ): TerminalCommand? {
        return walletProxy.saveTerminal(securityContext.jwtAuthentication().tokenValue(), body)
    }


    @PutMapping("/{uuid}")
    @Operation(
        summary = "Update terminal",
        description = """PUT /opex/v1/admin/terminal/{terminalUuid}.
Security: Bearer admin-token required. Required authority: ROLE_admin.
""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = TerminalCommand::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun updateTerminal(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "uuid", description = "Terminal UUID.", required = true)
        @PathVariable("uuid") terminalUuid: String,
        @RequestBody body: TerminalUpdateCommand
    ): TerminalCommand? {
        return walletProxy.updateTerminal(securityContext.jwtAuthentication().tokenValue(), terminalUuid, body)
    }

    @DeleteMapping("/{uuid}")
    @Operation(
        summary = "Delete terminal",
        description = """DELETE /opex/v1/admin/terminal/{terminalUuid}.
Security: Bearer admin-token required. Required authority: ROLE_admin.
""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "No response body.", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun deleteTerminal(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "uuid", description = "Terminal UUID.", required = true)
        @PathVariable("uuid") terminalUuid: String
    ) {
        walletProxy.deleteTerminal(securityContext.jwtAuthentication().tokenValue(), terminalUuid)
    }

    @GetMapping
    @Operation(
        summary = "Get terminal",
        description = """GET /opex/v1/admin/terminal.
Security: Bearer admin-token required. Required authority: ROLE_admin.
""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = TerminalCommand::class)))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun getTerminal(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): List<TerminalCommand>? {
        return walletProxy.getTerminals(securityContext.jwtAuthentication().tokenValue())
    }

    @GetMapping("/{uuid}")
    @Operation(
        summary = "Get terminal",
        description = """GET /opex/v1/admin/terminal/{terminalUuid}.
Security: Bearer admin-token required. Required authority: ROLE_admin.
""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = TerminalCommand::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun getTerminal(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "uuid", description = "Terminal UUID depending on the endpoint context.", required = true)
        @PathVariable("uuid") terminalUuid: String
    ): TerminalCommand? {
        return walletProxy.getTerminal(securityContext.jwtAuthentication().tokenValue(), terminalUuid)
    }

    @GetMapping("/{uuid}/gateway")
    @Operation(
        summary = "Get gateway(s) which the terminal is assigned to",
        description = """GET /opex/v1/admin/terminal/{gatewayUuid}/gateway.
Security: Bearer admin-token required. Required authority: ROLE_admin.
""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = CurrencyGatewayCommand::class)))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun getGatewayTerminal(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "uuid", description = "Terminal UUID.", required = true)
        @PathVariable("uuid") terminalUuid: String
    ): List<CurrencyGatewayCommand>? {
        return walletProxy.getAssignedGatewayToTerminal(securityContext.jwtAuthentication().tokenValue(), terminalUuid)
    }

    @PostMapping("/gateway/{uuid}")
    @Operation(
        summary = "Assign terminal to gateway",
        description = """POST /opex/v1/admin/terminal/gateway/{gatewayUuid}.
Validation: Request body is a raw JSON array of terminal UUID strings.
Security: Bearer admin-token required. Required authority: ROLE_admin.

""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "No response body.", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun assignTerminalToGateway(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "uuid", description = "Gateway UUID.", required = true)
        @PathVariable("uuid") gatewayUuid: String,
        @RequestBody terminal: List<String>
    ) {
        return walletProxy.assignTerminalsToGateway(
            securityContext.jwtAuthentication().tokenValue(),
            gatewayUuid,
            terminal
        )
    }

    @DeleteMapping("/gateway/{uuid}")
    @Operation(
        summary = "Revoke terminal from gateway",
        description = """DELETE /opex/v1/admin/terminal/gateway/{gatewayUuid}.
Validation: Request body is a raw JSON array of terminal UUID strings to revoke from the gateway.
Security: Bearer admin-token required. Required authority: ROLE_admin.
""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "No response body.", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun revokeTerminalFromGateway(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "uuid", description = "Gateway UUID.", required = true)
        @PathVariable("uuid") gatewayUuid: String,
        @RequestBody terminal: List<String>
    ) {
        return walletProxy.revokeTerminalsToGateway(
            securityContext.jwtAuthentication().tokenValue(),
            gatewayUuid,
            terminal
        )
    }

}
