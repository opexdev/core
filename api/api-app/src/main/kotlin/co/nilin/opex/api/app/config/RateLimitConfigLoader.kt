package co.nilin.opex.api.app.config

import co.nilin.opex.api.core.spi.RateLimitConfigService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class RateLimitConfigLoader(
    private val rateLimitConfig: RateLimitConfigService
) {
    @EventListener(ApplicationReadyEvent::class)
    fun preload() {
        CoroutineScope(Dispatchers.Default).launch {
            rateLimitConfig.loadConfig()
        }
    }
}