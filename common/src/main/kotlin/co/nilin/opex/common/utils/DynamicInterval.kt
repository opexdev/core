package co.nilin.opex.common.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit

class DynamicInterval(private val duration: Int, private val unit: TimeUnit, private val label: String? = null) {

    private fun offsetTime() = unit.toMillis(duration.toLong())

    fun timeInFuture() = Date().time + offsetTime()

    fun timeInPast() = Date().time - offsetTime()

    fun dateInFuture() = Date(timeInFuture())

    fun dateInPast() = Date(timeInPast())

    fun localDateTimeInFuture(): LocalDateTime = with(Instant.ofEpochMilli(timeInFuture())) {
        LocalDateTime.ofInstant(this, ZoneId.systemDefault())
    }

    fun localDateTimeInPast(): LocalDateTime = with(Instant.ofEpochMilli(timeInPast())) {
        LocalDateTime.ofInstant(this, ZoneId.systemDefault())
    }
}

fun Int.seconds() = DynamicInterval(this, TimeUnit.SECONDS)
fun Int.minutes() = DynamicInterval(this, TimeUnit.MINUTES)
fun Int.hours() = DynamicInterval(this, TimeUnit.HOURS)
fun Int.days() = DynamicInterval(this, TimeUnit.DAYS)