package co.nilin.opex.market.ports.postgres.util

import java.time.LocalDateTime

data class TimedCacheItem<T>(val cacheValue: T, val evictionTime: LocalDateTime)