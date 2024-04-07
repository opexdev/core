//package co.nilin.opex.wallet.app.service
//
//import co.nilin.opex.wallet.core.inout.CurrencyCommand
//import co.nilin.opex.wallet.core.model.Currencies
//import co.nilin.opex.wallet.core.model.Currency
//import co.nilin.opex.wallet.core.model.CurrencyImp
//import co.nilin.opex.wallet.core.model.PropagateCurrencyChanges
//import co.nilin.opex.wallet.core.model.otc.CurrencyImplementationResponse
//import co.nilin.opex.wallet.core.model.otc.FetchCurrencyInfo
//import co.nilin.opex.wallet.core.spi.BcGatewayProxy
//import co.nilin.opex.wallet.core.spi.CurrencyService
//import kotlinx.coroutines.runBlocking
//import org.springframework.stereotype.Service
//import org.springframework.transaction.annotation.Transactional
//import java.math.BigDecimal
//import java.util.UUID
//import java.util.stream.Collectors
//
//@Service
//class CurrencyService(
//        private val currencyService: CurrencyService,
//              private val bcGatewayProxy: BcGatewayProxy,
//
//        ) {
//
//
//    suspend fun createNewCurrency(request: CurrencyCommand): CurrencyCommand? {
//        return currencyService.createNewCurrency(
//           request.apply {
//               this.uuid=UUID.randomUUID().toString()
//               this.symbol=this.symbol.uppercase()
//               this.isCryptoCurrency=false
//           }
//        )
//    }
//    @Transactional
//    suspend fun addCurrency(request: CurrencyImp): Currency? {
//
//        val curr = currencyService.addCurrency(
//                Currency(
//                        request.symbol.uppercase(),
//                        request.name,
//                        request.precision,
//                        request.title,
//                        request.alias,
//                        request.maxDeposit,
//                        request.minDeposit,
//                        request.minWithdraw,
//                        request.maxWithdraw,
//                        request.icon,
//                        request.isTransitive,
//                        request.isActive,
//                        request.sign,
//                        request.description,
//                        request.shortDescription
//                )
//
//        )
//        var imp: CurrencyImplementationResponse? = null
//        if (request.isValidForPropagatingOnChain()) {
//            imp = bcGatewayProxy.createCurrency(
//                    PropagateCurrencyChanges(
//                            request.symbol.uppercase(),
//                            request.name,
//                            request.implementationSymbol!!,
//                            request.newChain,
//                            request.tokenName,
//                            request.tokenAddress,
//                            request.isToken,
//                            request.withdrawFee!!,
//                            request.minWithdraw!!,
//                            request.isWithdrawEnabled,
//                            request.decimal!!,
//                            request.chain!!
//                    )
//            )
//        }
//
//
//        return curr?.apply {
//            currencyImpData = FetchCurrencyInfo(co.nilin.opex.wallet.core.model.otc.Currency(request.name, request.symbol),
//                    listOf(imp))
//        }
//
//    }
//
//
//    @Transactional
//    suspend fun updateCurrency(request: CurrencyImp): Currency? {
//        val curr = currencyService.updateCurrency(
//                Currency(
//                        request.symbol.uppercase(),
//                        request.name,
//                        request.precision,
//                        request.title,
//                        request.alias,
//                        request.maxDeposit,
//                        request.minDeposit,
//                        request.minWithdraw,
//                        request.maxWithdraw,
//                        request.icon,
//                        request.isTransitive,
//                        request.isActive,
//                        request.sign,
//                        request.description,
//                        request.shortDescription
//                )
//
//        )
//
//        var imp: CurrencyImplementationResponse? = null
//        if (request.isValidForPropagatingOnChain()) {
//            bcGatewayProxy.updateCurrency(
//                    PropagateCurrencyChanges(
//                            request.symbol.uppercase(),
//                            request.name,
//                            request.implementationSymbol!!,
//                            request.newChain,
//                            request.tokenName,
//                            request.tokenAddress,
//                            request.isToken,
//                            request.withdrawFee!!,
//                            request.minWithdraw!!,
//                            request.isWithdrawEnabled,
//                            request.decimal!!,
//                            request.chain!!
//                    )
//            )
//        }
//        return curr?.apply {
//            currencyImpData = FetchCurrencyInfo(co.nilin.opex.wallet.core.model.otc.Currency(request.name, request.symbol),
//                    listOf(imp))
//        }
//
//    }
//
//    @Transactional
//    suspend fun fetchCurrency(symbol: String): Currency? {
//        return currencyService.getCurrency(symbol.uppercase())
//                ?.let {
//                    it.apply { currencyImpData = bcGatewayProxy.getCurrencyInfo(symbol.uppercase()) }
//                }
//    }
//
//    @Transactional
//    suspend fun fetchCurrencies(): Currencies? {
//        return Currencies(currencyService.getCurrencies()
//                .currencies?.stream()
//                ?.map {
//                    it.apply {
//                        currencyImpData = runBlocking {
//                            bcGatewayProxy.getCurrencyInfo(it.symbol.uppercase())
//                        }
//                    }
//                }
//                ?.collect(Collectors.toList()))
//    }
//
//
//}