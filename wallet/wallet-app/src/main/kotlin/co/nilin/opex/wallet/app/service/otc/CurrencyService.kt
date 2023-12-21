package co.nilin.opex.wallet.app.service.otc

import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.core.model.CurrencyImp
import co.nilin.opex.wallet.core.model.PropagateCurrencyChanges
import co.nilin.opex.wallet.core.spi.BcGatewayProxy
import co.nilin.opex.wallet.core.spi.CurrencyService
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

//propagate currency's info changes
@Service
class CurrencyService(
    private val currencyService: CurrencyService,
    private val bcGatewayProxy: BcGatewayProxy
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
            bcGatewayProxy.createCurrency(
                PropagateCurrencyChanges(
                    request.symbol.uppercase(),
                    request.name,
                    request.implementationSymbol,
                    request.chain,
                    request.tokenName,
                    request.tokenAddress,
                    request.isToken,
                    request.withdrawFee,
                    request.minimumWithdraw,
                    request.isWithdrawEnabled,
                    request.decimal
                )
            )
            it
        }
    }


}