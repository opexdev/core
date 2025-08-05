package co.nilin.opex.market.ports.postgres.model

import org.springframework.data.annotation.Id
import java.time.LocalDate

class UserRangedAction(
    val userId: String,
    val action: RangedAction,
    val interval: RangedInterval,
    val startDate: LocalDate,
    val endDate: LocalDate,
    @Id val id: Long? = null
)

enum class RangedAction {
    TOTAL_VOLUME,
}

enum class RangedInterval {
    DAY,
    WEEK,
    MONTH,
}