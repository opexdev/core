package co.nilin.opex.common.translation

import co.nilin.opex.common.data.MessageTranslation
import co.nilin.opex.common.data.UserLanguage
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Service
class TranslationCacheService(
        private val configClient: ConfigClient?,
        private val messageSource: MessageSource?
) {

    private val cache: MutableMap<Pair<String, UserLanguage>, MessageTranslation> = ConcurrentHashMap()
    private var lastUpdate: Long = System.currentTimeMillis()
    private var job: Job? = null
    private val logger = LoggerFactory.getLogger(TranslationCacheService::class.java)


    @PostConstruct
    fun start() {
        job = CoroutineScope(Dispatchers.IO).launch {
            logger.info("Going to get messages which are updated after {}", lastUpdate)
            while (isActive) {
                if (configClient != null) {
                    val newMessages = configClient.getMessagesUpdatedAfter(cache?.let { lastUpdate })
                    newMessages?.forEach { msg ->
                        cache[Pair(msg.key, msg.language)] =
                                MessageTranslation(msg.key, msg.message, msg.language)
                    }
                }
                lastUpdate = System.currentTimeMillis()
                delay(30_000)
            }
        }
    }


    fun getMessage(key: String, userLanguage: String): String? {
        val cached = cache[Pair(key, UserLanguage.safeValueOf(userLanguage))]
        if (cached != null) {
            return cached.message
        }
        return try {
            messageSource?.getMessage(key, null, Locale(userLanguage.lowercase()))
        } catch (ex: Exception) {
            key
        }
    }

    @PreDestroy
    fun stop() {
        job?.cancel()
    }


}
