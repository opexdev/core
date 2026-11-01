package co.nilin.opex.common.service

import co.nilin.opex.common.data.MessageTranslation
import co.nilin.opex.common.data.UserLanguage
import co.nilin.opex.common.proxy.CustomMessageClient
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
class TranslationCacheService(
    private val customMessageClient: CustomMessageClient?,
    private val messageSource: MessageSource?
) : InitializingBean, DisposableBean {

    private val cache: MutableMap<Pair<String, UserLanguage>, MessageTranslation> =
        ConcurrentHashMap()

    private var lastUpdate: Long? = null
    private var job: Job? = null
    private val logger = LoggerFactory.getLogger(TranslationCacheService::class.java)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun afterPropertiesSet() {
        start()
    }

    fun start() {
        job = scope.launch {
            logger.info(
                "Going to get messages which are updated after: {}",
                lastUpdate
            )

            while (isActive) {
                try {
                    customMessageClient
                        ?.getMessagesUpdatedAfter(lastUpdate)
                        ?.forEach { msg ->
                            cache[Pair(msg.key, msg.language)] =
                                MessageTranslation(msg.key, msg.message, msg.language)
                        }

                    lastUpdate = System.currentTimeMillis()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logger.error("Error fetching messages", e)
                }

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

    override fun destroy() {
        stop()
    }

    fun stop() {
        scope.cancel()
    }
}