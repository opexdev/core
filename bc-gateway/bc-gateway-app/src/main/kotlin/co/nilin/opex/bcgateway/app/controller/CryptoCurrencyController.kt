package co.nilin.opex.bcgateway.app.controller

import co.nilin.opex.bcgateway.app.dto.AddCurrencyRequest
import co.nilin.opex.bcgateway.core.model.CryptoCurrencyCommand
import co.nilin.opex.bcgateway.core.model.CurrencyImps
import co.nilin.opex.bcgateway.core.spi.CryptoCurrencyHandlerV2
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/crypto-currency")
class CryptoCurrencyController(val cryptoCurrencyHandler: CryptoCurrencyHandlerV2) {

    @GetMapping("/{currencyUuid}/impls")
    suspend fun fetchCurrencyImpls(@PathVariable("currencyUuid") currency: String): CurrencyImps {
        return cryptoCurrencyHandler.fetchCurrencyInfo(currency)
    }



    @GetMapping("/impls")
    suspend fun fetchCurrenciesImpls(): CurrencyInfo {
        return cryptoCurrencyHandler.fetchCurrencyInfo(currency)
    }



    @PostMapping("/{currencyUuid}/imp/{impUuid}")
    suspend fun addCurrencyInfo(
        @PathVariable("currencyUuid") currencySymbol: String,
        @PathVariable("impUuid") impUuid:String,
        @RequestBody request: CryptoCurrencyCommand
    ): CryptoCurrencyCommand? {
       cryptoCurrencyHandler
    }

    @PutMapping("/{currency}")
    suspend fun updateCurrencyInfo(
        @PathVariable("currency") currencySymbol: String,
        @RequestBody addCurrencyRequest: AddCurrencyRequest
    ):CurrencyImplementation? {
        addCurrencyRequest.currencySymbol = currencySymbol
        with(addCurrencyRequest) {

           return currencyHandler.updateCurrencyImplementation(this.currencySymbol!!,
                this.implementationSymbol,
                this.currencyName,
                this.newChain,
                this.tokenName,
                this.tokenAddress,
                this.isToken!!,
                this.withdrawFee,
                this.minimumWithdraw,
                this.isWithdrawEnabled!!,
                this.decimal,
                this.chain
            )
        }
    }

    @GetMapping("/chains")
    suspend fun getNetworks(@RequestParam(required = false) currency: String?): List<CurrencyImplementation> {
        return if (currency != null)
            currencyHandler.findImplementationsByCurrency(currency)
        else
            currencyHandler.fetchAllImplementations()
    }
}
