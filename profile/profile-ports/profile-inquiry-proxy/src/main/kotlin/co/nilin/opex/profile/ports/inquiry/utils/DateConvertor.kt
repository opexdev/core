package co.nilin.opex.profile.ports.inquiry.utils

import com.ibm.icu.util.Calendar
import com.ibm.icu.util.TimeZone
import com.ibm.icu.util.ULocale
import java.util.*

fun Long.toPersianDateFormatted(): String {
    val date = Date(this)
    val calendar = Calendar.getInstance(ULocale("fa_IR@calendar=persian")).apply {
        timeZone = TimeZone.getTimeZone("Asia/Tehran")
        time = date
    }

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    return String.format("%04d%02d%02d", year, month, day)
}
