package co.nilin.opex.device.core.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.device.core.data.ActiveUserStat
import co.nilin.opex.device.core.data.DailyLoginStat
import co.nilin.opex.device.core.spi.SessionPersister
import co.nilin.opex.device.core.spi.UserDevicePersister
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class LoginStatService(
    private val sessionPersister: SessionPersister,
    private val userDevicePersister: UserDevicePersister,
) {

    suspend fun dailyLogins(fromMillis: Long, toMillis: Long): List<DailyLoginStat> {
        val (from, to) = validatedRange(fromMillis, toMillis)
        return sessionPersister.countDailyLogins(from, to)
    }

    suspend fun activeUsers(fromMillis: Long, toMillis: Long): ActiveUserStat {
        val (from, to) = validatedRange(fromMillis, toMillis)
        val count = userDevicePersister.countActiveUsers(from, to)
        return ActiveUserStat(
            userCount = count,
        )
    }

    private fun validatedRange(fromMillis: Long, toMillis: Long): Pair<LocalDateTime, LocalDateTime> {
        if (toMillis < fromMillis)
            throw OpexError.InvalidRequestParam.exception("'to' is before 'from'")

        val from = fromMillis.toLocalDateTime()
        val to = toMillis.toLocalDateTime()

        return from to to
    }

    private fun Long.toLocalDateTime(): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
}
