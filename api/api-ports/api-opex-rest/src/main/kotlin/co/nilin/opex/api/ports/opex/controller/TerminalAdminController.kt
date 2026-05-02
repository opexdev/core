package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.CurrencyGatewayCommand
import co.nilin.opex.api.core.inout.TerminalCommand
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/opex/v1/admin/terminal")
class TerminalAdminController(
    private val walletProxy: WalletProxy,
) {
    @PostMapping
    suspend fun registerTerminal(
        @CurrentSecurityContext securityContext: SecurityContext, @RequestBody body: TerminalCommand
    ): TerminalCommand? {
        return walletProxy.saveTerminal(securityContext.jwtAuthentication().tokenValue(), body)
    }


    @PutMapping("/{uuid}")
    suspend fun updateTerminal(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("uuid") terminalUuid: String,
        @RequestBody body: TerminalCommand
    ): TerminalCommand? {
        return walletProxy.updateTerminal(securityContext.jwtAuthentication().tokenValue(), terminalUuid, body)
    }

    @DeleteMapping("/{uuid}")
    suspend fun deleteTerminal(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("uuid") terminalUuid: String,
    ) {
        walletProxy.deleteTerminal(securityContext.jwtAuthentication().tokenValue(), terminalUuid)
    }

    @GetMapping
    suspend fun getTerminal(
        @CurrentSecurityContext securityContext: SecurityContext,
    ): List<TerminalCommand>? {
        return walletProxy.getTerminals(securityContext.jwtAuthentication().tokenValue())
    }

    @GetMapping("/{uuid}")
    suspend fun getTerminal(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("uuid") terminalUuid: String,
    ): TerminalCommand? {
        return walletProxy.getTerminal(securityContext.jwtAuthentication().tokenValue(), terminalUuid)
    }

    @GetMapping("/{uuid}/gateway")
    suspend fun getGatewayTerminal(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("uuid") terminalUuid: String,
    ): List<CurrencyGatewayCommand>? {
        return walletProxy.getAssignedGatewayToTerminal(securityContext.jwtAuthentication().tokenValue(), terminalUuid)
    }
}
