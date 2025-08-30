package co.nilin.opex.common.translation

import co.nilin.opex.common.data.MessageTranslation
import co.nilin.opex.utility.error.data.UserLanguage
import org.springframework.context.MessageSource
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
class TranslationCacheService(
        private val configClient: ConfigClient?,
        private val messageSource: MessageSource?
) {

    private val cache: MutableMap<Pair<String, UserLanguage>, MessageTranslation> = ConcurrentHashMap()
    private var lastUpdate: Long = System.currentTimeMillis()

    fun getMessage(key: String, userLanguage: String?): String? {
        val cached = cache[Pair(key, UserLanguage.safeValueOf(userLanguage))]
        if (cached != null) {
            return cached.message
        }
        return try {
            messageSource?.getMessage(key, null, Locale(userLanguage))
        } catch (ex: Exception) {
            key
        }
    }

    @Scheduled(fixedRate = 30 * 1000)
    suspend fun refreshCache() {
        if (configClient == null) return
        val newMessages = configClient.getMessagesUpdatedAfter(cache?.let { lastUpdate })
        newMessages?.forEach { msg ->
            cache[Pair(msg.key, msg.language)] =
                    MessageTranslation(msg.key, msg.message, msg.language)
        }
        lastUpdate = System.currentTimeMillis()
    }
}
