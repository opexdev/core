package co.nilin.opex.wallet.core.inout

import java.math.BigDecimal
import java.time.LocalDate

data class DailyAmount(
    val date: LocalDate,
    val totalAmount: BigDecimal
)
