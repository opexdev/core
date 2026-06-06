package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.AdminSearchDepositRequest
import co.nilin.opex.wallet.app.dto.ManualTransferRequest
import co.nilin.opex.wallet.app.dto.TerminalLocalizationResponse
import co.nilin.opex.wallet.app.service.DepositService
import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.spi.GatewayTerminalManager
import co.nilin.opex.wallet.core.spi.TerminalManager
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@RestController
@RequestMapping("/admin/terminal")

class TerminalAdminController(
    private val depositService: DepositService,
    private val terminalManager: TerminalManager,
    private val gatewayTerminalManager: GatewayTerminalManager
) {

    @PostMapping("")
    suspend fun registerTerminal(
        @RequestBody body: TerminalCommand
    ): TerminalCommand? {
        return terminalManager.save(body.apply { uuid = UUID.randomUUID().toString() })
    }


    @PutMapping("/{uuid}")
    suspend fun updateTerminal(
        @PathVariable("uuid") terminalUuid: String,
        @RequestBody body: TerminalCommand
    ): TerminalCommand? {
        return terminalManager.update(body.apply { uuid = terminalUuid })
    }


    @DeleteMapping("/{uuid}")
    suspend fun deleteTerminal(
        @PathVariable("uuid") terminalUuid: String,
    ) {
        terminalManager.delete(terminalUuid)
    }

    @GetMapping("")
    suspend fun getTerminal(
    ): List<TerminalCommand>? {
        return terminalManager.fetchTerminal()
    }

    @GetMapping("/{uuid}")
    suspend fun getTerminal(
        @PathVariable("uuid") terminalUuid: String,
    ): TerminalCommand? {
        return terminalManager.fetchTerminal(terminalUuid)
    }

    @GetMapping("/{uuid}/gateway")
    suspend fun getGatewayTerminal(
        @PathVariable("uuid") terminalUuid: String,
    ): List<CurrencyGatewayCommand>? {
        return gatewayTerminalManager.getAssignedGatewayToTerminal(terminalUuid)
    }

    @PostMapping("/{uuid}/localization")
    suspend fun saveTerminalLocalization(
        @PathVariable("uuid") terminalUuid: String,
        @RequestBody terminalLocalizations: List<TerminalLocalizationCommand>
    ): TerminalLocalizationResponse {
        val terminalLocalizations = terminalManager.saveTerminalLocalizations(terminalUuid, terminalLocalizations)
        return TerminalLocalizationResponse(terminalUuid, terminalLocalizations)
    }

    @GetMapping("/{uuid}/localization")
    suspend fun getTerminalLocalization(
        @PathVariable("uuid") terminalUuid: String
    ): TerminalLocalizationResponse {
        val terminalLocalizations = terminalManager.fetchTerminalLocalizations(terminalUuid)
        return TerminalLocalizationResponse(terminalUuid, terminalLocalizations)
    }

    @DeleteMapping("/terminal/localization/{id}")
    suspend fun deleteTerminalLocalization(
        @PathVariable("id") id: Long,
    ) {
        terminalManager.deleteTerminalLocalizations(id)
    }
}