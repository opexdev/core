package co.nilin.opex.market.app.controller

import co.nilin.opex.market.core.spi.MarketQueryHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/stats")
class MarketStatsController(private val marketQueryHandler: MarketQueryHandler) {

    @GetMapping("/overall")
    suspend fun overall(@RequestParam since: Long) {

    }

}