package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.CurrencyGatewayCommand
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/opex/v1/admin")
class GatewayAdminController(
    private val walletProxy: WalletProxy,
) {
    @PostMapping("/{currencySymbol}/gateway")
    suspend fun addGatewayToCurrency(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("currencySymbol") currencySymbol: String,
        @RequestBody body: CurrencyGatewayCommand,
    ): CurrencyGatewayCommand? {
        return walletProxy.addCurrencyToGateway(securityContext.jwtAuthentication().tokenValue(), currencySymbol, body)
    }


    @PutMapping("/{currencySymbol}/gateway/{uuid}")
    suspend fun updateGateway(
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
    suspend fun getGateway(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("uuid") gatewayUuid: String,
        @PathVariable("currencySymbol") currencySymbol: String,
    ): CurrencyGatewayCommand? {
        return walletProxy.getGateway(
            securityContext.jwtAuthentication().tokenValue(),
            gatewayUuid,
            currencySymbol
        )
    }

    @DeleteMapping("/{currencySymbol}/gateway/{uuid}")
    suspend fun deleteGateway(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("uuid") gatewayUuid: String,
        @PathVariable("currencySymbol") currencySymbol: String,
    ) {
        walletProxy.deleteGateway(securityContext.jwtAuthentication().tokenValue(), gatewayUuid, currencySymbol)
    }

}
