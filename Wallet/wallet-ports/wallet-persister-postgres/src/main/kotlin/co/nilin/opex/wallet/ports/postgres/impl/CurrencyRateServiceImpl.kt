package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRateRepository
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.core.spi.CurrencyRateService
import kotlinx.coroutines.reactive.awaitFirstOrDefault
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class CurrencyRateServiceImpl(val currencyRateRepository: CurrencyRateRepository) : CurrencyRateService {
    override suspend fun convert(amount: Amount, targetCurrency: Currency): BigDecimal {
        if (amount.currency.getName() == targetCurrency.getName())
            return amount.amount

        var rate = currencyRateRepository.findBySourceAndDest(
            amount.currency.getName(), targetCurrency.getName()
        )
            .map { BigDecimal.valueOf(it!!.rate) }
            .awaitFirstOrNull()
        if (rate != null) {
            rate = currencyRateRepository.findBySourceAndDest(
                targetCurrency.getName(), amount.currency.getName()
            )
                .map { BigDecimal.valueOf(it!!.rate) }
                .awaitFirstOrDefault(BigDecimal.ZERO)
        }
        return amount.amount.multiply(rate)
    }
}