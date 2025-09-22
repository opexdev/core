package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.CurrenciesDto
import co.nilin.opex.wallet.app.dto.CurrencyDto
import co.nilin.opex.wallet.app.service.CurrencyServiceV2
import co.nilin.opex.wallet.core.inout.CurrencyData
import co.nilin.opex.wallet.core.inout.CurrencyGatewayCommand
import co.nilin.opex.wallet.core.inout.GatewayType
import co.nilin.opex.wallet.core.inout.TerminalCommand
import co.nilin.opex.wallet.core.model.QuoteCurrency
import co.nilin.opex.wallet.core.spi.GatewayTerminalManager
import co.nilin.opex.wallet.core.spi.QuoteCurrencyManager
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/currency")
class CurrencyController(
    private val currencyService: CurrencyServiceV2,
    private val gatewayTerminalManager: GatewayTerminalManager,
    private val quoteCurrencyManager: QuoteCurrencyManager,
) {

    @PostMapping("")
    suspend fun addCurrency(@RequestBody request: CurrencyDto): CurrencyDto? {
        return currencyService.createNewCurrency(request)
    }

    @PutMapping("/{currencySymbol}")
    suspend fun updateCurrency(
        @PathVariable("currencySymbol") currencySymbol: String,
        @RequestBody request: CurrencyDto,
    ): CurrencyDto? {
        return currencyService.updateCurrency(request.apply { symbol = currencySymbol })
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
    ): CurrencyGatewayCommand? {
        return currencyService.addGateway2Currency(request.apply {
            this.currencySymbol = currencySymbol
        })
    }

    @PutMapping("{currencySymbol}/gateway/{gatewayUuid}")
    suspend fun updateGateway(
        @PathVariable("gatewayUuid") gatewayUuid: String,
        @PathVariable("currencySymbol") currencySymbol: String,
        @RequestBody request: CurrencyGatewayCommand,
    ): CurrencyGatewayCommand? {
        return currencyService.updateGateway(request.apply {
            this.currencySymbol = currencySymbol
            this.gatewayUuid = gatewayUuid
        })
    }

    @GetMapping("{currencySymbol}/gateway/{gatewayUuid}")
    suspend fun getGateway(
        @PathVariable("gatewayUuid") gatewayUuid: String,
        @PathVariable("currencySymbol") currencySymbol: String,
    ): CurrencyGatewayCommand? {
        return currencyService.fetchCurrencyGateway(gatewayUuid, currencySymbol)
    }

    @DeleteMapping("{currencySymbol}/gateway/{gatewayUuid}")
    suspend fun deleteGateway(
        @PathVariable("gatewayUuid") gatewayUuid: String,
        @PathVariable("currencySymbol") currencySymbol: String,
    ) {
        currencyService.deleteGateway(gatewayUuid, currencySymbol)
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
    ) {
        return gatewayTerminalManager.assignTerminalsToGateway(gatewayUuid, terminal)
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
    ) {
        return gatewayTerminalManager.revokeTerminalsToGateway(gatewayUuid, terminal)
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
    ) {
        quoteCurrencyManager.update(currency, isReference, displayOrder)
    }

}