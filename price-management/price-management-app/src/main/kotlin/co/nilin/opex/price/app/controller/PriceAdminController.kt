package co.nilin.opex.price.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.price.core.dto.PairRateConfigView
import co.nilin.opex.price.core.dto.ProviderPrice
import co.nilin.opex.price.core.dto.UpsertPairRateConfigRequest
import co.nilin.opex.price.core.service.PairRateConfigAdminManager
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/admin/rate-config")
class PriceAdminController(
    private val pairRateConfigAdminManager: PairRateConfigAdminManager
) {

    @GetMapping
    suspend fun list(): List<PairRateConfigView> =
        pairRateConfigAdminManager.getConfigs()

    @GetMapping("/{symbol}")
    suspend fun get(@PathVariable symbol: String): PairRateConfigView =
        pairRateConfigAdminManager.getConfig(symbol) ?: throw OpexError.RateConfigNotFound.exception()

    /**
     * Creates or fully replaces a symbol's config, its selected providers, and — when priceMode
     * is MANUAL and a price is included — its price, all in one call.
     */
    @PostMapping
    suspend fun upsert(@RequestBody request: UpsertPairRateConfigRequest): PairRateConfigView =
        pairRateConfigAdminManager.upsertConfig(request)

    @GetMapping("/{symbol}/providers")
    suspend fun getProvidersPrice(@PathVariable symbol: String): List<ProviderPrice> {
        return pairRateConfigAdminManager.getProvidersPrice(symbol)
    }
}