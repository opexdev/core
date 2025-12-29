package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.analytics.ActivityTotals
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.*
import kotlin.random.Random

@RestController
@RequestMapping("/opex/v1/analytics")
class UserAnalyticsController {

    @GetMapping("/user-activity")
    suspend fun userActivity(@CurrentSecurityContext securityContext: SecurityContext): Map<String, ActivityTotals> {
        val jwt = securityContext.jwtAuthentication().token
        val uuid = jwt.subject ?: "unknown"

        val zone = ZoneId.systemDefault()
        val todayStart = LocalDate.now(zone).atStartOfDay(zone).toInstant()

        val days = 30
        val result = LinkedHashMap<String, ActivityTotals>(days)

        // Initial balance seeded by user hash
        var runningBalance = BigDecimal.valueOf(100 + (uuid.hashCode() and Int.MAX_VALUE) % 900L)

        for (i in (days - 1) downTo 0) {
            val dayInstant = todayStart.minusSeconds(86400L * i)
            val dayKey = dayInstant.toEpochMilli().toString()

            // deterministic seed per user+day
            val seed = deterministicSeed(uuid.hashCode(), dayInstant.toEpochMilli())
            val rnd = Random(seed)

            val base = BigDecimal.valueOf((50 + rnd.nextInt(0, 950)).toLong()) // 50..999

            val deposit = scaleMoney(base.multiply(BigDecimal.valueOf(rnd.nextDouble(0.0, 5.0))))
            val withdraw = scaleMoney(deposit.multiply(BigDecimal.valueOf(rnd.nextDouble(0.0, 0.9))))

            val trade = scaleMoney(base.multiply(BigDecimal.valueOf(rnd.nextDouble(0.5, 8.0))))
            val swap = scaleMoney(base.multiply(BigDecimal.valueOf(rnd.nextDouble(0.0, 3.0))))

            val pnlDrift = scaleMoney(trade.multiply(BigDecimal.valueOf(rnd.nextDouble(-0.01, 0.01))))

            runningBalance = (runningBalance + deposit - withdraw + pnlDrift).coerceAtLeast(BigDecimal.ZERO)

            result[dayKey] = ActivityTotals(
                    totalBalance = runningBalance.min(MAX_BALANCE),
                    totalWithdraw = withdraw,
                    totalDeposit = deposit,
                    totalTrade = trade.min(MAX_TRADE),
                    totalSwap = swap.min(MAX_SWAP)
            )
        }

        return result
    }

    private fun scaleMoney(v: BigDecimal) = v.setScale(2, RoundingMode.HALF_UP)

    private val MAX_BALANCE = BigDecimal("10000000")
    private val MAX_TRADE = BigDecimal("250000")
    private val MAX_SWAP = BigDecimal("100000")

    /**
     * Simple deterministic seed generator for a user and day.
     * Combines user hash and day epoch millis into a reproducible seed.
     */
    private fun deterministicSeed(userHash: Int, dayEpochMillis: Long): Long {
        var seed = userHash.toLong() * 31 + dayEpochMillis
        // simple bit mixing
        seed = seed xor (seed shr 33)
        seed *= 0xBF58476D1CE4E5BL
        seed = seed xor (seed shr 33)
        seed *= 0xc4ceb9fe1a85ec5L
        seed = seed xor (seed shr 33)
        return seed
    }

}
