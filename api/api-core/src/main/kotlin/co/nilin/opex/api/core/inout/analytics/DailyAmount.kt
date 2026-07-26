package co.nilin.opex.api.core.inout.analytics

import java.math.BigDecimal
import java.time.LocalDate

data class DailyAmount(val date: LocalDate, var totalAmount: BigDecimal)
