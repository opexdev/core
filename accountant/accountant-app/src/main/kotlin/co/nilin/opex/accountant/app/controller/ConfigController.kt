package co.nilin.opex.accountant.app.controller

import co.nilin.opex.accountant.core.model.FeeConfig
import co.nilin.opex.accountant.core.model.PairConfig
import co.nilin.opex.accountant.core.spi.FeeConfigService
import co.nilin.opex.accountant.core.spi.PairConfigLoader
import co.nilin.opex.matching.engine.core.model.OrderDirection
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/config")
class ConfigController(
    private val pairConfigLoader: PairConfigLoader,
    private val feeConfigService: FeeConfigService
) {

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
}