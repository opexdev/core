package co.nilin.opex.wallet.core.model

import co.nilin.opex.wallet.core.inout.CurrencyCommand
import java.math.BigDecimal

data class Amount(val currency: CurrencyCommand, val amount: BigDecimal)