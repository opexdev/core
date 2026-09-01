package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.core.model.otc.Rate
import co.nilin.opex.wallet.core.service.otc.RateService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


//TODO
@RestController
@RequestMapping("/internal/otc")
class InternalRateController(
    private val rateService: RateService
) {

    @PostMapping("/rate")
    suspend fun upsertRate(@RequestBody rate: Rate): Rate {
        return rateService.upsertRate(rate)
    }
}
