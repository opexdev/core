package co.nilin.opex.market.app.service

import co.nilin.opex.common.utils.DynamicInterval
import co.nilin.opex.market.ports.postgres.util.RedisCacheHelper
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill

import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.TimeUnit

@Component
class RateLimiterService(
    private val redisCacheHelper: RedisCacheHelper
) {

    private val buckets: MutableMap<String, Bucket> = mutableMapOf()

    private fun createBucket(maxRequests: Int, windowInSeconds: Long): Bucket {
        val limit = Bandwidth.classic(
            maxRequests.toLong(),
            Refill.greedy(maxRequests.toLong(), Duration.ofSeconds(windowInSeconds))
        )
        return Bucket.builder().addLimit(limit).build()
    }

    fun checkRateLimit(key: String, maxRequests: Int, windowInSeconds: Int, apiPath: String): Boolean {
        val redisKey = "rate-limit:$key-$apiPath"

        val storedTokenCount: Long? = redisCacheHelper.get(redisKey)
        val bucket = buckets.computeIfAbsent(redisKey) { createBucket(maxRequests, windowInSeconds.toLong()) }

        if (storedTokenCount == null) {
            bucket.reset()
            redisCacheHelper.put(redisKey, maxRequests.toLong(), DynamicInterval(windowInSeconds, TimeUnit.SECONDS))
        } else {
            val tokensToAdd = storedTokenCount - bucket.availableTokens
            if (tokensToAdd > 0) {
                bucket.addTokens(tokensToAdd)
            }
        }

        val allowed = bucket.tryConsume(1)

        if (allowed) {
            redisCacheHelper.put(redisKey, bucket.availableTokens, DynamicInterval(windowInSeconds, TimeUnit.SECONDS))
        }

        return allowed
    }
}