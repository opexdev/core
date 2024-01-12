package co.nilin.opex.wallet.app.service.otc

import co.nilin.opex.wallet.core.model.Currencies
import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.core.model.CurrencyImp
import co.nilin.opex.wallet.core.model.PropagateCurrencyChanges
import co.nilin.opex.wallet.core.model.otc.FetchCurrencyInfo
import co.nilin.opex.wallet.core.model.otc.LoginRequest
import co.nilin.opex.wallet.core.spi.AuthProxy
import co.nilin.opex.wallet.core.spi.BcGatewayProxy
import co.nilin.opex.wallet.core.spi.CurrencyService
import kotlinx.coroutines.runBlocking
import org.springframework.context.annotation.ConditionContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Collectors


//propagate currency's info changes
//TODO find a better name, I just renamed it to avoid the conflict of names which is confusing for later usages
@Service
class OTCCurrencyService(
        private val currencyService: CurrencyService,
        //TODO: let's do it event based (kafka) or in a more general way, we shouldn't assume a direct dependency between wallet and bc-gateway
        //and also as we need to create wallet for admins for new currency, it completely makes sense to put them all together in one place, or maybe as some listener to currency_add event!
        private val bcGatewayProxy: BcGatewayProxy,

) {

    @Transactional
    suspend fun addCurrency(request: CurrencyImp): Currency? {

        return currencyService.addCurrency(
                Currency(
                        request.symbol.uppercase(),
                        request.name,
                        request.precision,
                        request.title,
                        request.alias,
                        request.maxDeposit,
                        request.minDeposit,
                        request.minWithdraw,
                        request.maxWithdraw,
                        request.icon,
                        request.isTransitive,
                        request.isActive,
                        request.sign,
                        request.description,
                        request.shortDescription
                )

        )?.let {
            val imp = bcGatewayProxy.createCurrency(
                    PropagateCurrencyChanges(
                            request.symbol.uppercase(),
                            request.name,
                            request.implementationSymbol,
                            request.newChain,
                            request.tokenName,
                            request.tokenAddress,
                            request.isToken,
                            request.withdrawFee,
                            request.minimumWithdraw,
                            request.isWithdrawEnabled,
                            request.decimal,
                            request.chain
                    )
            )
            it.apply {
                currencyImpData = FetchCurrencyInfo(co.nilin.opex.wallet.core.model.otc.Currency(request.name, request.symbol),
                        listOf(imp))
            }
        }
    }


    @Transactional
    suspend fun updateCurrency(request: CurrencyImp): Currency? {
        return currencyService.updateCurrency(
                Currency(
                        request.symbol.uppercase(),
                        request.name,
                        request.precision,
                        request.title,
                        request.alias,
                        request.maxDeposit,
                        request.minDeposit,
                        request.minWithdraw,
                        request.maxWithdraw,
                        request.icon,
                        request.isTransitive,
                        request.isActive,
                        request.sign,
                        request.description,
                        request.shortDescription
                )

        )?.let {

            val imp = bcGatewayProxy.updateCurrency(
                    PropagateCurrencyChanges(
                            request.symbol.uppercase(),
                            request.name,
                            request.implementationSymbol,
                            request.newChain,
                            request.tokenName,
                            request.tokenAddress,
                            request.isToken,
                            request.withdrawFee,
                            request.minimumWithdraw,
                            request.isWithdrawEnabled,
                            request.decimal,
                            request.chain
                    )
            )
            it.apply {
                currencyImpData = FetchCurrencyInfo(co.nilin.opex.wallet.core.model.otc.Currency(request.name, request.symbol),
                        listOf(imp))
            }
        }
    }

    @Transactional
    suspend fun fetchCurrency(symbol: String): Currency? {
        return currencyService.getCurrency(symbol.uppercase())
                ?.let {
                    it.apply { currencyImpData = bcGatewayProxy.getCurrencyInfo(symbol.uppercase()) } }
    }

    @Transactional
    suspend fun fetchCurrencies(): Currencies? {
        return Currencies(currencyService.getCurrencies()
                .currencies?.stream()
                ?.map { it.apply { currencyImpData = runBlocking {
                    bcGatewayProxy.getCurrencyInfo(it.symbol.uppercase()) } } }
                ?.collect(Collectors.toList()))
    }



}