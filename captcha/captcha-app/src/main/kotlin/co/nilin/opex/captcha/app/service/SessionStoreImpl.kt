package co.nilin.opex.captcha.app.service

import co.nilin.opex.captcha.app.api.SessionStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SessionStoreImpl(
    private val map: MutableMap<String, Long>,
    @Value("\${app.captcha-window-seconds}") private val captchaWindowSeconds: Long
) : SessionStore {
    override fun put(proof: String) {
        cleanExpired()
        map[proof] = System.currentTimeMillis()
    }

    override fun remove(proof: String): Boolean = map.remove(proof)?.let { true } ?: false

    override fun contains(proof: String): Boolean = proof in map

    private fun cleanExpired() {
        val ms = System.currentTimeMillis()
        map.filterValues { it <= ms - captchaWindowSeconds * 1000 }.forEach { map.remove(it.key) }
    }
}
