package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.api.WithdrawActivityManager
import co.nilin.opex.accountant.core.model.DailyAmount
import co.nilin.opex.accountant.core.spi.UserWithdrawVolumePersister
import co.nilin.opex.common.utils.CacheManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
@Service
class WithdrawActivityManagerImpl(
    private val cacheManager: CacheManager<String, BigDecimal>,
    private val withdrawVolumePersister: UserWithdrawVolumePersister,
    @Value("\${app.zone-offset}") private val zoneOffsetString: String,
) : WithdrawActivityManager {
    override suspend fun getLastDaysWithdrawActivity(
        userId: String,
        quoteCurrency: String?,
        n: Int
    ): List<DailyAmount> {
        val today = LocalDate.now(ZoneOffset.of(zoneOffsetString))
        val dates = (0..n - 1).map { today.minusDays(it.toLong()) }

        val result = mutableMapOf<LocalDate, BigDecimal>()
        val missingDates = mutableListOf<LocalDate>()

        for (date in dates) {
            val cacheKey = "withdraw:daily:$userId:$date"
            val cached = cacheManager.get(cacheKey)

            if (cached != null) {
                result[date] = cached
            } else {
                missingDates.add(date)
            }
        }

        if (missingDates.isNotEmpty()) {
            val startDate = missingDates.minOrNull()!!

            val dbData = withdrawVolumePersister.getLastDaysWithdraw(userId, startDate,quoteCurrency)
                .stream().collect(Collectors.toMap(DailyAmount::date, DailyAmount::totalAmount));

            for (date in missingDates) {
                val value = dbData[date] ?: BigDecimal.ZERO
                val (ttl, unit) = ttlFor(date, today)
                val cacheKey = "withdraw:daily:$userId:$date"

                cacheManager.put(cacheKey, value, ttl, unit)
                result[date] = value
            }
        }

        return result
            .map { DailyAmount(it.key, it.value) }
            .sortedBy { it.date }

    }


    private fun ttlFor(date: LocalDate, today: LocalDate): Pair<Long, TimeUnit> =
        if (date == today) {
            15L to TimeUnit.MINUTES
        } else {
            100L to TimeUnit.DAYS
        }

}