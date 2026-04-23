package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/opex/v1/admin/localization")
class LocalizationAdminController(
    private val walletProxy: WalletProxy,
) {

    @GetMapping("/currency/{currency}")
    suspend fun getCurrencyLocalizations(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("currency") currency: String
    ): CurrencyLocalizationResponse {
        return walletProxy.getCurrencyLocalizations(securityContext.jwtAuthentication().tokenValue(), currency)
    }

    @PostMapping("/currency/{currency}")
    suspend fun saveCurrencyLocalizations(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("currency") currency: String,
        @RequestBody request: CurrencyLocalizationRequest
    ): CurrencyLocalizationResponse {
        return walletProxy.saveCurrencyLocalizations(
            securityContext.jwtAuthentication().tokenValue(),
            currency,
            request
        )
    }

    @PutMapping("/currency")
    suspend fun updateCurrencyLocalization(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: CurrencyLocalizationCommand
    ): CurrencyLocalizationCommand {
        return walletProxy.updateCurrencyLocalization(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @DeleteMapping("/currency/{id}")
    suspend fun deleteCurrencyLocalization(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("id") id: Long
    ) {
        walletProxy.deleteCurrencyLocalization(securityContext.jwtAuthentication().tokenValue(), id)
    }

    @GetMapping("/terminal/{terminalUuid}")
    suspend fun getTerminalLocalizations(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("terminalUuid") terminalUuid: String
    ): TerminalLocalizationResponse {
        return walletProxy.getTerminalLocalizations(securityContext.jwtAuthentication().tokenValue(), terminalUuid)
    }

    @PostMapping("/terminal/{terminalUuid}")
    suspend fun saveTerminalLocalizations(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("terminalUuid") terminalUuid: String,
        @RequestBody request: TerminalLocalizationRequest
    ): TerminalLocalizationResponse {
        return walletProxy.saveTerminalLocalizations(
            securityContext.jwtAuthentication().tokenValue(),
            terminalUuid,
            request
        )
    }

    @PutMapping("/terminal")
    suspend fun updateTerminalLocalization(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: TerminalLocalizationCommand
    ): TerminalLocalizationCommand {
        return walletProxy.updateTerminalLocalization(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @DeleteMapping("/terminal/{id}")
    suspend fun deleteTerminalLocalization(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("id") id: Long
    ) {
        walletProxy.deleteTerminalLocalization(securityContext.jwtAuthentication().tokenValue(), id)
    }
}