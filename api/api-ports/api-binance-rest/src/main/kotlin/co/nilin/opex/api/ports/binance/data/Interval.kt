package co.nilin.opex.api.ports.binance.data

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit

enum class Interval(val label: String, val unit: TimeUnit, val duration: Long) {

    Minute("1m", TimeUnit.MINUTES, 1),
    ThreeMinutes("3m", TimeUnit.MINUTES, 3),
    FiveMinutes("5m", TimeUnit.MINUTES, 5),
    FifteenMinutes("15m", TimeUnit.MINUTES, 15),
    ThirtyMinutes("30m", TimeUnit.MINUTES, 30),
    Hour("1h", TimeUnit.HOURS, 1),
    TwoHours("2h", TimeUnit.HOURS, 2),
    FourHours("4h", TimeUnit.HOURS, 4),
    SixHours("6h", TimeUnit.HOURS, 6),
    EightHours("8h", TimeUnit.HOURS, 8),
    TwelveHours("12h", TimeUnit.HOURS, 12),
    TwentyFourHours("24h", TimeUnit.HOURS, 24),
    Day("1d", TimeUnit.DAYS, 1),
    ThreeDays("3d", TimeUnit.DAYS, 3),
    Week("1w", TimeUnit.DAYS, 7),
    Month("1M", TimeUnit.DAYS, 31),
    ThreeMonth("3M", TimeUnit.DAYS, 90);

    private fun getOffsetTime() = unit.toMillis(duration)

    fun getDate() = Date(Date().time - getOffsetTime())

    fun getLocalDateTime(): LocalDateTime = with(Instant.ofEpochMilli(getDate().time)) {
        LocalDateTime.ofInstant(this, ZoneId.systemDefault())
    }

    companion object {
        fun findByLabel(label: String): Interval? {
            return values().find { it.label == label }
        }
    }

}