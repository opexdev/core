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

@RestController
@RequestMapping("/opex/v1/admin")
class LocalizationAdminController(
    private val walletProxy: WalletProxy,
    private val blockchainGatewayProxy: BlockchainGatewayProxy
) {
    @GetMapping("/currency/{currency}/localization")
    suspend fun getCurrencyLocalizations(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("currency") currency: String
    ): CurrencyLocalizationResponse {
        return walletProxy.getCurrencyLocalizations(securityContext.jwtAuthentication().tokenValue(), currency)
    }

    @PostMapping("/currency/{currency}/localization")
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
    suspend fun deleteCurrencyLocalization(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("id") id: Long
    ) {
        walletProxy.deleteCurrencyLocalization(securityContext.jwtAuthentication().tokenValue(), id)
    }

    @GetMapping("/terminal/{terminalUuid}/localization")
    suspend fun getTerminalLocalizations(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("terminalUuid") terminalUuid: String
    ): TerminalLocalizationResponse {
        return walletProxy.getTerminalLocalizations(securityContext.jwtAuthentication().tokenValue(), terminalUuid)
    }

    @PostMapping("/terminal/{terminalUuid}/localization")
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
    suspend fun deleteTerminalLocalization(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("id") id: Long
    ) {
        walletProxy.deleteTerminalLocalization(securityContext.jwtAuthentication().tokenValue(), id)
    }

    @GetMapping("/gateway/{gatewayUuid}/localization")
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