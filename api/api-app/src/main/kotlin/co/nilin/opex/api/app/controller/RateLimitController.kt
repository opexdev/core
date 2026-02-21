package co.nilin.opex.api.app.controller

import co.nilin.opex.api.core.spi.RateLimitConfigService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/rate-limit")
class RateLimitController(
    private val rateLimitConfig: RateLimitConfigService,
) {
    @PostMapping
    suspend fun reloadRateLimits() {
        rateLimitConfig.loadConfig()
    }
}