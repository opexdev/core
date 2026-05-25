package co.nilin.opex.common.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit

enum class LimitedInterval(val label: String, val unit: TimeUnit, val duration: Long) {

    Day("1d", TimeUnit.DAYS, 1),
    Week("1w", TimeUnit.DAYS, 7),
    Month("1M", TimeUnit.DAYS, 31),
    Year("1Y", TimeUnit.DAYS, 365);

    private fun getOffsetTime() = unit.toMillis(duration)

    fun getDate() = Date(Date().time - getOffsetTime())

    fun getTime() = Date().time - getOffsetTime()

    fun getLocalDateTime(): LocalDateTime = with(Instant.ofEpochMilli(getDate().time)) {
        LocalDateTime.ofInstant(this, ZoneId.systemDefault())
    }

    companion object {
        fun findByLabel(label: String): LimitedInterval? {
            return values().find { it.label == label }
        }
    }

}