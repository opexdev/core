package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.CurrencyCommand
import co.nilin.opex.wallet.core.model.Amount
import java.math.BigDecimal

interface CurrencyRateService {
    suspend fun convert(amount: Amount, targetCurrency: CurrencyCommand): BigDecimal
}
