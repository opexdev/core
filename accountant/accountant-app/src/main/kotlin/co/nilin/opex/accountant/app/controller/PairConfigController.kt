package co.nilin.opex.accountant.app.controller

import co.nilin.opex.accountant.app.data.PairFeeResponse
import co.nilin.opex.accountant.core.model.FeeConfig
import co.nilin.opex.accountant.core.model.PairConfig
import co.nilin.opex.accountant.core.model.PairFeeConfig
import co.nilin.opex.accountant.core.spi.FeeConfigService
import co.nilin.opex.accountant.core.spi.PairConfigLoader
import co.nilin.opex.common.OpexError
import co.nilin.opex.matching.engine.core.model.OrderDirection
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/config")
class PairConfigController(
    private val pairConfigLoader: PairConfigLoader,
    private val feeConfigService: FeeConfigService
) {

    @GetMapping("/{pair}/fee/{direction}-{userLevel}")
    suspend fun fetchPairFeeConfig(
        @PathVariable("pair") pair: String,
        @PathVariable("direction") direction: OrderDirection,
        @PathVariable("userLevel") level: String
    ): PairFeeConfig {
        return pairConfigLoader.load(pair, direction, level)
    }

    @GetMapping("/{pair}/{direction}")
    suspend fun fetchPairConfig(
        @PathVariable("pair") pair: String,
        @PathVariable("direction") direction: OrderDirection
    ): PairConfig {
        return pairConfigLoader.load(pair, direction)
    }

    @GetMapping("/all")
    suspend fun fetchPairConfigs(): List<PairConfig> {
        return pairConfigLoader.loadPairConfigs()
    }

    @GetMapping("/fee")
    suspend fun getFeeConfigs(): List<FeeConfig> {
        return feeConfigService.loadFeeConfigs()
    }

    @GetMapping("/fee/{pair}")
    suspend fun getFeeConfig(
        @PathVariable pair: String,
        @RequestParam(required = false) direction: OrderDirection?,
        @RequestParam(required = false) userLevel: String?
    ): PairFeeResponse {
        val fee = pairConfigLoader.loadPairFeeConfigs(pair, direction ?: OrderDirection.BID, userLevel ?: "*")
            ?: throw OpexError.PairFeeNotFound.exception()
        return PairFeeResponse(fee.pairConfig.pair, fee.direction, fee.userLevel, fee.makerFee, fee.takerFee)
    }
}