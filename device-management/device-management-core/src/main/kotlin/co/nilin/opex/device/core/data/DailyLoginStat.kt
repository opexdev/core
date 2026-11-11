package co.nilin.opex.device.core.data

import java.time.LocalDate

data class DailyLoginStat(
    val date: LocalDate,
    val userCount: Long
)
