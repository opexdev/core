package co.nilin.opex.auth.service

import co.nilin.opex.auth.data.ActionCache
import co.nilin.opex.auth.data.ActionType
import co.nilin.opex.auth.model.CaptchaType
import co.nilin.opex.auth.proxy.CaptchaProxy
import co.nilin.opex.auth.utils.CacheManager
import co.nilin.opex.common.OpexError
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class CaptchaHandler(
    private val cacheManager: CacheManager<String, ActionCache>,
    private val captchaProxy: CaptchaProxy
) {
    suspend fun validateCaptchaWithActionCache(
        username: String,
        captchaCode: String?,
        captchaType: CaptchaType?,
        action: ActionType,
        maxAttempts: Int = 3,
        expireTimeMinutes: Long = 10
    ) {
        val cache = cacheManager.get(username)

        if (cache == null || cache.actionType != action || cache.remainingAttempts <= 0) {
            captchaProxy.validateCaptcha(captchaCode ?: throw OpexError.CaptchaRequired.exception(), captchaType ?: CaptchaType.INTERNAL)
            cacheManager.put(username, ActionCache(action, maxAttempts), expireTimeMinutes, TimeUnit.MINUTES)
            return
        }
        cacheManager.put(
            username,
            cache.copy(remainingAttempts = cache.remainingAttempts - 1),
            expireTimeMinutes,
            TimeUnit.MINUTES
        )
    }
}
