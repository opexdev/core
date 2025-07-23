package co.nilin.opex.profile.ports.inquiry.utils

import com.ibm.icu.util.Calendar
import com.ibm.icu.util.ULocale
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

fun LocalDateTime.toPersianDateFormatted(): String {
    val instant = this.atZone(ZoneId.systemDefault()).toInstant()
    val date = Date.from(instant)
    val calendar = Calendar.getInstance(ULocale("fa_IR@calendar=persian"))
    calendar.time = date

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    return String.format("%04d%02d%02d", year, month, day)
}