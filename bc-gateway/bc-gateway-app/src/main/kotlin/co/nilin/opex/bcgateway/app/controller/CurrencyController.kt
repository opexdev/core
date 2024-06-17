package co.nilin.opex.bcgateway.app.controller

import co.nilin.opex.bcgateway.app.dto.AddCurrencyRequest
import co.nilin.opex.bcgateway.core.model.CurrencyImplementation
import co.nilin.opex.bcgateway.core.model.CurrencyInfo
import co.nilin.opex.bcgateway.core.spi.CurrencyHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/currency")
class CurrencyController(val currencyHandler: CurrencyHandler) {

//    @GetMapping("/{currency}")
//    suspend fun fetchCurrencyInfo(@PathVariable("currency") currency: String): CurrencyInfo {
//        return currencyHandler.fetchCurrencyInfo(currency)
//    }


//    @PostMapping("/{currency}")
//    suspend fun addCurrencyInfo(
//        @PathVariable("currency") currencySymbol: String,
//        @RequestBody addCurrencyRequest: AddCurrencyRequest
//    ): CurrencyImplementation? {
//        addCurrencyRequest.currencySymbol = currencySymbol
//        with(addCurrencyRequest) {
//
//       return  currencyHandler.addCurrencyImplementationV2(this.currencySymbol!!,
//             this.implementationSymbol,
//             this.currencyName,
//             this.chain,
//             this.tokenName,
//             this.tokenAddress,
//             this.isToken!!,
//             this.withdrawFee,
//             this.minimumWithdraw,
//             this.isWithdrawEnabled!!,
//             this.decimal
//             )
//        }
//    }

//    @PutMapping("/{currency}")
//    suspend fun updateCurrencyInfo(
//        @PathVariable("currency") currencySymbol: String,
//        @RequestBody addCurrencyRequest: AddCurrencyRequest
//    ):CurrencyImplementation? {
//        addCurrencyRequest.currencySymbol = currencySymbol
//        with(addCurrencyRequest) {
//
//           return currencyHandler.updateCurrencyImplementation(this.currencySymbol!!,
//                this.implementationSymbol,
//                this.currencyName,
//                this.newChain,
//                this.tokenName,
//                this.tokenAddress,
//                this.isToken!!,
//                this.withdrawFee,
//                this.minimumWithdraw,
//                this.isWithdrawEnabled!!,
//                this.decimal,
//                this.chain
//            )
//        }
//    }

//    @GetMapping("/chains")
//    suspend fun getNetworks(@RequestParam(required = false) currency: String?): List<CurrencyImplementation> {
//        return if (currency != null)
//            currencyHandler.findImplementationsByCurrency(currency)
//        else
//            currencyHandler.fetchAllImplementations()
//    }
}
