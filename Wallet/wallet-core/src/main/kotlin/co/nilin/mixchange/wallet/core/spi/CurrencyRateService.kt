package co.nilin.mixchange.wallet.core.spi

import co.nilin.mixchange.wallet.core.model.Currency
import co.nilin.mixchange.wallet.core.model.Amount
import java.math.BigDecimal

interface CurrencyRateService {
    suspend fun convert(amount: Amount, targetCurrency: Currency): BigDecimal
}
