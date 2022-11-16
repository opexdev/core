package co.nilin.opex.api.app.data

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit

enum class APIKeyExpiration(private val unit: TimeUnit, private val duration: Long) {

    ONE_MONTH(TimeUnit.DAYS, 30),
    THREE_MONTHS(TimeUnit.DAYS, 90),
    SIX_MONTHS(TimeUnit.DAYS, 180),
    ONE_YEAR(TimeUnit.DAYS, 365);

    private fun getDate() = Date(Date().time + unit.toMillis(duration))

    fun getLocalDateTime(): LocalDateTime = with(Instant.ofEpochMilli(getDate().time)) {
        LocalDateTime.ofInstant(this, ZoneId.systemDefault())
    }

}