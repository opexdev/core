package co.nilin.opex.api.ports.opex.service

import co.nilin.opex.api.core.inout.ResolveUsersRequest
import co.nilin.opex.api.core.spi.ProfileProxy
import co.nilin.opex.common.utils.LoggerDelegate
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Component
class OwnerNameResolver(
    private val profileProxy: ProfileProxy,
) {
    private data class CacheEntry(val name: String?, val expiresAt: Long)

    private val logger by LoggerDelegate()

    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private val ttl: Duration = Duration.ofDays(1)

    suspend fun resolve(token: String, uuids: Set<String>): Map<String, String?> {
        if (uuids.isEmpty()) return emptyMap()

        val now = Instant.now().toEpochMilli()
        val cached = mutableMapOf<String, String?>()
        val missing = mutableListOf<String>()

        uuids.forEach { uuid ->
            val entry = cache[uuid]
            if (entry != null && entry.expiresAt > now) {
                cached[uuid] = entry.name
            } else {
                missing.add(uuid)
            }
        }

        if (missing.isNotEmpty()) {
            try {
                val result = profileProxy.resolveUsers(token, ResolveUsersRequest(missing))
                val expiry = Instant.now().plus(ttl).toEpochMilli()
                result.filter { (uuid, name) -> name != null }.forEach { (uuid, name) ->
                    cache[uuid] = CacheEntry(name, expiry)
                }
                val notReturned = missing.filterNot { result.containsKey(it) }
                notReturned.forEach { uuid -> cache[uuid] = CacheEntry(null, expiry) }

                cached.putAll(result)
            } catch (t: Throwable) {
                logger.debug("Error in fetching users data $t")
            }
        }

        return cached
    }
}
