package co.nilin.opex.device.core.data

import java.time.LocalDate

data class ActiveUserStat(
    val from: LocalDate,
    val to: LocalDate,
    val userCount: Long,
)
