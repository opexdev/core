//package co.nilin.opex.wallet.app.service.otc
//
//import co.nilin.opex.wallet.core.model.Currencies
//import co.nilin.opex.wallet.core.model.Currency
//import co.nilin.opex.wallet.core.model.CurrencyImp
//import co.nilin.opex.wallet.core.model.PropagateCurrencyChanges
//import co.nilin.opex.wallet.core.model.otc.FetchCurrencyInfo
//import co.nilin.opex.wallet.core.spi.BcGatewayProxy
//import co.nilin.opex.wallet.core.spi.CurrencyService
//import kotlinx.coroutines.runBlocking
//import org.springframework.stereotype.Service
//import org.springframework.transaction.annotation.Transactional
//import java.util.stream.Collectors
//
////propagate currency's info changes
//@Service
//class CurrencyService(
//        private val currencyService: CurrencyService,
//        private val bcGatewayProxy: BcGatewayProxy
//) {
//
//    @Transactional
//    suspend fun addCurrency(request: CurrencyImp): Currency? {
//        return currencyService.addCurrency(
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
//        )?.let {
//            val imp = bcGatewayProxy.createCurrency(
//                    PropagateCurrencyChanges(
//                            request.symbol.uppercase(),
//                            request.name,
//                            request.implementationSymbol,
//                            request.newChain,
//                            request.tokenName,
//                            request.tokenAddress,
//                            request.isToken,
//                            request.withdrawFee,
//                            request.minimumWithdraw,
//                            request.isWithdrawEnabled,
//                            request.decimal,
//                            request.chain
//                    )
//            )
//            it.apply {
//                currencyImpData = FetchCurrencyInfo(co.nilin.opex.wallet.core.model.otc.Currency(request.name, request.symbol),
//                        listOf(imp))
//            }
//        }
//    }
//
//
//    @Transactional
//    suspend fun updateCurrency(request: CurrencyImp): Currency? {
//        return currencyService.updateCurrency(
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
//        )?.let {
//            val imp = bcGatewayProxy.updateCurrency(
//                    PropagateCurrencyChanges(
//                            request.symbol.uppercase(),
//                            request.name,
//                            request.implementationSymbol,
//                            request.newChain,
//                            request.tokenName,
//                            request.tokenAddress,
//                            request.isToken,
//                            request.withdrawFee,
//                            request.minimumWithdraw,
//                            request.isWithdrawEnabled,
//                            request.decimal,
//                            request.chain
//                    )
//            )
//            it.apply {
//                currencyImpData = FetchCurrencyInfo(co.nilin.opex.wallet.core.model.otc.Currency(request.name, request.symbol),
//                        listOf(imp))
//            }
//        }
//    }
//
//    @Transactional
//    suspend fun fetchCurrency(symbol: String): Currency? {
//        return currencyService.getCurrency(symbol)
//                ?.let { it.apply { currencyImpData = bcGatewayProxy.getCurrencyInfo(symbol) } }
//    }
//
//    @Transactional
//    suspend fun fetchCurrencies(): Currencies? {
//        return Currencies(currencyService.getCurrencies()
//                .currencies?.stream()
//                ?.map { it.apply { currencyImpData = runBlocking { bcGatewayProxy.getCurrencyInfo(it.symbol) } } }
//                ?.collect(Collectors.toList()))
//    }
//}