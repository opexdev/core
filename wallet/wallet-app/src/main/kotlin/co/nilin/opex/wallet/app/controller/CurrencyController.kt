package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.CurrenciesDto
import co.nilin.opex.wallet.app.dto.CurrencyDto
import co.nilin.opex.wallet.app.dto.CurrencyLocalizationResponse
import co.nilin.opex.wallet.app.dto.UpdateCurrencyRequest
import co.nilin.opex.wallet.app.service.CurrencyServiceV2
import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.model.QuoteCurrency
import co.nilin.opex.wallet.core.spi.GatewayTerminalManager
import co.nilin.opex.wallet.core.spi.QuoteCurrencyManager
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/currency")
class CurrencyController(
    private val currencyService: CurrencyServiceV2,
    private val gatewayTerminalManager: GatewayTerminalManager,
    private val quoteCurrencyManager: QuoteCurrencyManager,
) {

    @PostMapping("")
    suspend fun addCurrency(
        @RequestBody request: CurrencyDto,
        @CurrentSecurityContext securityContext: SecurityContext
    ): CurrencyDto? {
        return currencyService.createNewCurrency(request, securityContext.authentication.name)
    }

    @PutMapping("/{currencySymbol}")
    suspend fun updateCurrency(
        @PathVariable("currencySymbol") currencySymbol: String,
        @RequestBody request: UpdateCurrencyRequest,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): CurrencyDto? {
        return currencyService.updateCurrency(
            request.apply { symbol = currencySymbol }.toCurrencyDto(),
            securityContext.authentication.name
        )
    }


    @GetMapping("/{currencySymbol}")
    suspend fun getCurrency(
        @PathVariable("currencySymbol") currencySymbol: String,
        @RequestParam includeOffChainGateways: Boolean? = false,
        @RequestParam includeOnChainGateways: Boolean? = false,
    ): CurrencyDto? {

        val includeGateways = mutableListOf<GatewayType>().apply {
            if (includeOffChainGateways == true) add(GatewayType.OffChain)
            if (includeOnChainGateways == true) add(GatewayType.OnChain)
        }
        return currencyService.fetchCurrencyWithGateways(currencySymbol, includeGateways)
    }

    @GetMapping("")
    suspend fun getCurrencies(
        @RequestParam includeOffChainGateways: Boolean? = false,
        @RequestParam includeOnChainGateways: Boolean? = false,
    ): CurrenciesDto? {
        val includeGateways = mutableListOf<GatewayType>().apply {

            if (includeOffChainGateways == true) add(GatewayType.OffChain)
            if (includeOnChainGateways == true) add(GatewayType.OnChain)
        }
        return currencyService.fetchCurrenciesWithGateways(includeGateways)
    }

    @GetMapping("/all")
    suspend fun getCurrencies(): List<CurrencyData> {
        return currencyService.fetchCurrencies()
    }

    @PostMapping("/{currencySymbol}/gateway")
    suspend fun addGateway2Currency(
        @PathVariable("currencySymbol") currencySymbol: String,
        @RequestBody request: CurrencyGatewayCommand,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): CurrencyGatewayCommand? {
        return currencyService.addGateway2Currency(request.apply {
            this.currencySymbol = currencySymbol
        }, securityContext.authentication.name)
    }

    @PutMapping("/{currencySymbol}/gateway/{gatewayUuid}")
    suspend fun updateGateway(
        @PathVariable("gatewayUuid") gatewayUuid: String,
        @PathVariable("currencySymbol") currencySymbol: String,
        @RequestBody request: CurrencyGatewayCommand,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): CurrencyGatewayCommand? {
        return currencyService.updateGateway(request.apply {
            this.currencySymbol = currencySymbol
            this.gatewayUuid = gatewayUuid
        }, securityContext.authentication.name)
    }

    @GetMapping("/{currencySymbol}/gateway/{gatewayUuid}")
    suspend fun getGateway(
        @PathVariable("gatewayUuid") gatewayUuid: String,
        @PathVariable("currencySymbol") currencySymbol: String,
    ): CurrencyGatewayCommand? {
        return currencyService.fetchCurrencyGateway(gatewayUuid, currencySymbol)
    }

    @DeleteMapping("/{currencySymbol}/gateway/{gatewayUuid}")
    suspend fun deleteGateway(
        @PathVariable("gatewayUuid") gatewayUuid: String,
        @PathVariable("currencySymbol") currencySymbol: String,
        @CurrentSecurityContext securityContext: SecurityContext,
    ) {
        currencyService.deleteGateway(gatewayUuid, currencySymbol, securityContext.authentication.name)
    }


    @GetMapping("/gateways")
    suspend fun getGateways(
        @RequestParam includeOffChainGateways: Boolean? = false,
        @RequestParam includeOnChainGateways: Boolean? = false,
    ): List<CurrencyGatewayCommand>? {
        val includeGateways = mutableListOf<GatewayType>().apply {
            if (includeOffChainGateways == true) add(GatewayType.OffChain)
            if (includeOnChainGateways == true) add(GatewayType.OnChain)
        }
        return currencyService.fetchGateways(includeGateways)
    }


    @PostMapping("/gateway/{gatewayUuid}/terminal")
    suspend fun assignTerminalToGateway(
        @PathVariable("gatewayUuid") gatewayUuid: String,
        @RequestBody terminal: List<String>,
        @CurrentSecurityContext securityContext: SecurityContext,
    ) {
        return gatewayTerminalManager.assignTerminalsToGateway(gatewayUuid, terminal, securityContext.authentication.name)
    }


    @GetMapping("/gateway/{gatewayUuid}/terminal")
    suspend fun getGatewayTerminal(
        @PathVariable("gatewayUuid") gatewayUuid: String,
    ): List<TerminalCommand>? {
        return gatewayTerminalManager.getAssignedTerminalToGateway(gatewayUuid)
    }

    @DeleteMapping("/gateway/{gatewayUuid}/terminal")
    suspend fun revokeTerminalFromGateway(
        @PathVariable("gatewayUuid") gatewayUuid: String,
        @RequestBody terminal: List<String>,
        @CurrentSecurityContext securityContext: SecurityContext,
    ) {
        return gatewayTerminalManager.revokeTerminalsToGateway(gatewayUuid, terminal, securityContext.authentication.name)
    }

    @GetMapping("/quotes")
    suspend fun getQuoteCurrencies(
        @RequestParam isReference: Boolean?,
    ): List<QuoteCurrency> {
        return quoteCurrencyManager.getAll(isReference)
    }

    @PutMapping("/quote/{currency}")
    suspend fun updateQuoteCurrency(
        @PathVariable("currency") currency: String,
        @RequestParam isReference: Boolean,
        @RequestParam displayOrder: Int,
        @CurrentSecurityContext securityContext: SecurityContext,
    ) {
        quoteCurrencyManager.update(currency, isReference, displayOrder, securityContext.authentication.name)
    }

    @GetMapping("/{currency}/localization")
    suspend fun getCurrencyLocalizations(@PathVariable("currency") currency: String): CurrencyLocalizationResponse {
        val localizations = currencyService.fetchCurrencyLocalizations(currency)
        return CurrencyLocalizationResponse(currency, localizations)
    }

    @PostMapping("/{currency}/localization")
    suspend fun saveCurrencyLocalizations(
        @PathVariable("currency") currency: String,
        @RequestBody currencyLocalizations: List<CurrencyLocalizationCommand>,
        @CurrentSecurityContext securityContext: SecurityContext
    ): CurrencyLocalizationResponse {
        val localizations = currencyService.saveCurrencyLocalizations(currency, currencyLocalizations, securityContext.authentication.name)
        return CurrencyLocalizationResponse(currency, localizations)
    }

    @DeleteMapping("/localization/{id}")
    suspend fun deleteCurrencyLocalization(
        @PathVariable("id") id: Long,
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        currencyService.deleteCurrencyLocalization(id, securityContext.authentication.name)
    }
}
