package co.nilin.opex.profile.ports.inquiry.utils

import com.ibm.icu.util.Calendar
import com.ibm.icu.util.TimeZone
import com.ibm.icu.util.ULocale
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

private val persianLocale = ULocale("fa_IR@calendar=persian")
private val tehranTimeZone = TimeZone.getTimeZone("Asia/Tehran")

private fun formatPersianDate(date: Date): String {
    val calendar = Calendar.getInstance(persianLocale).apply {
        timeZone = tehranTimeZone
        time = date
    }
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    return String.format("%04d%02d%02d", year, month, day)
}

fun Long.toPersianDateFormatted(): String =
    formatPersianDate(Date(this))

fun LocalDateTime.toPersianDateFormatted(): String =
    formatPersianDate(Date.from(this.atZone(ZoneId.of("Asia/Tehran")).toInstant()))