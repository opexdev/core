package co.nilin.opex.market.app.utils

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

fun LocalDateTime.asDate(): Date {
    return Date.from(atZone(ZoneId.systemDefault()).toInstant())
}

fun Date.asLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(toInstant(), ZoneId.systemDefault())
}