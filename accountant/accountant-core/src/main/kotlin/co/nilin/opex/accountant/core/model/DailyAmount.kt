package co.nilin.opex.accountant.core.model

import java.math.BigDecimal
import java.time.LocalDate

data class DailyAmount(
    val date: LocalDate,
    val totalAmount: BigDecimal
)
