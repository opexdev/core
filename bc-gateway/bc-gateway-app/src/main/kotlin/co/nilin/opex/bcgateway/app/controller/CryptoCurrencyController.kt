package co.nilin.opex.bcgateway.app.controller

import co.nilin.opex.bcgateway.app.dto.ChainResponse
import co.nilin.opex.bcgateway.core.model.*
import co.nilin.opex.bcgateway.core.spi.ChainLoader
import co.nilin.opex.bcgateway.core.spi.CryptoCurrencyHandlerV2
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/crypto-currency")
class CryptoCurrencyController(val cryptoCurrencyHandler: CryptoCurrencyHandlerV2, private val chainLoader: ChainLoader) {

    @PostMapping("/{currencySymbol}/impl")
    suspend fun addNewCurrencyImpl(
            @PathVariable("currencySymbol") currencySymbol: String,
            @RequestBody request: CryptoCurrencyCommand
    ): CryptoCurrencyCommand? {
        return cryptoCurrencyHandler.createImpl(request.apply {
            this.implUuid = UUID.randomUUID().toString()
            this.currencySymbol = currencySymbol
        })
    }


    @PutMapping("/{currency}/impl/{implUuid}")
    suspend fun updateCurrencyImpl(
            @PathVariable("currency") currencySymbol: String,
            @PathVariable("implUuid") implUuid: String,
            @RequestBody request: CryptoCurrencyCommand
    ): CryptoCurrencyCommand? {
        return cryptoCurrencyHandler.updateImpl(request.apply {
            this.currencySymbol = currencySymbol
            this.implUuid = implUuid
        })
    }


    @DeleteMapping("/{currency}/impl/{implUuid}")
    suspend fun deleteCurrencyImpl(
            @PathVariable("currency") currencySymbol: String,
            @PathVariable("implUuid") implUuid: String,
    ):Void? {
       return cryptoCurrencyHandler.deleteImpl(
                implUuid, currencySymbol)
    }

    @GetMapping("/impls")
    suspend fun fetchImpls(@RequestParam("currency") currencySymbol: String? = null): CurrencyImps? {
        return cryptoCurrencyHandler.fetchCurrencyImpls(FetchImpls(currencySymbol = currencySymbol))
    }

    @GetMapping("/{currency}/impls")
    suspend fun fetchCurrencyImpls(@PathVariable("currency") currencySymbol: String): CurrencyImps? {
        return cryptoCurrencyHandler.fetchCurrencyImpls(FetchImpls(currencySymbol = currencySymbol))
    }

    @GetMapping("/{currency}/impl/{implUuid}")
    suspend fun fetchSpecificImpl(@PathVariable("implUuid") implUuid: String,
                                  @PathVariable("currency") currencySymbol: String): CurrencyImps? {
        return cryptoCurrencyHandler.fetchCurrencyImpls(FetchImpls(implUuid = implUuid, currencySymbol = currencySymbol))
    }

    @GetMapping("/chain")
    suspend fun getChains(): List<ChainResponse> {
        return chainLoader.fetchAllChains().map { c -> ChainResponse(c.name, c.addressTypes.map { it.type }) }
    }

}
