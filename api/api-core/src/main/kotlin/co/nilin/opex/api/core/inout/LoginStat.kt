package co.nilin.opex.api.core.inout

import java.time.LocalDate

data class DailyLoginStat(
    val date: LocalDate,
    val userCount: Long
)

data class ActiveUserStat(
    val from: LocalDate,
    val to: LocalDate,
    val userCount: Long,
)
