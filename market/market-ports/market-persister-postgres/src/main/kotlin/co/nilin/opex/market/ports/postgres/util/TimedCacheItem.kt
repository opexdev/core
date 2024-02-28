package co.nilin.opex.market.ports.postgres.util

data class TimedCacheItem(val cacheValue: Any, val evictionTime: Long)