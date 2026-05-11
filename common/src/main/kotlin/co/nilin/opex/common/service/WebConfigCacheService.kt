package co.nilin.opex.common.service

import co.nilin.opex.common.data.WebConfig
import co.nilin.opex.common.proxy.ConfigClient
import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

object GlobalWebConfigCache {
    @Volatile
    var webConfig: WebConfig? = null
}

@Service
class WebConfigService(
    private val configClient: ConfigClient,
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