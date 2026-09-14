package co.nilin.opex.price.core.spi

import co.nilin.opex.price.core.dto.CrossRateSparkline
import java.time.LocalDateTime

interface CrossRateSparklineLoader {

    /**
     * For every base asset that appears in `rate_history` (except [refCurrency]), reconstructs
     * the `<asset>-<refCurrency>` price series over [startTime, endTime], sampled into [points]
     * evenly spaced buckets. Assets are priced through [hub] (direct pair or its inverse) and
     * then divided, so `<asset>-<refCurrency> = (asset in hub) / (refCurrency in hub)`.
     *
     * All of the reconstruction — bucketing, last-value-carried-forward, the division, the
     * change % and the trend flag — is done in a single SQL query. Pairs that cannot be
     * resolved (or end up with fewer than 2 points) are omitted.
     */
    suspend fun loadSparklines(
        refCurrency: String,
        hub: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        points: Int,
    ): List<CrossRateSparkline>
}
