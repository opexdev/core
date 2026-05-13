package co.nilin.opex.common.service

import co.nilin.opex.common.data.WebConfig
import co.nilin.opex.common.proxy.CustomUserLanguageClient
import kotlinx.coroutines.runBlocking
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

object GlobalWebConfigCache {
    @Volatile
    var webConfig: WebConfig? = null
}

@ConditionalOnProperty(name = ["app.custom-user-language.enabled"], havingValue = "true", matchIfMissing = false)
@Service
class WebConfigService(
    private val configClient: CustomUserLanguageClient,
) {

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    fun refreshWebConfig() = runBlocking {
        try {
            val webConfig: WebConfig = configClient.getWebConfig()

            GlobalWebConfigCache.webConfig = webConfig

        } catch (e: Exception) {
            println("WebConfig refresh failed: ${e.message}")
        }
    }
}