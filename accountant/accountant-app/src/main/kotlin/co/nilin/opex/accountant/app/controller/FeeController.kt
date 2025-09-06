package co.nilin.opex.accountant.app.controller

import co.nilin.opex.accountant.core.api.FeeCalculator
import co.nilin.opex.accountant.core.model.FeeConfig
import co.nilin.opex.accountant.core.model.UserFee
import co.nilin.opex.accountant.core.spi.FeeConfigService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/fee")
class FeeController(
    private val feeConfigService: FeeConfigService,
    private val feeCalculator: FeeCalculator,
) {
    @GetMapping("/config")
    suspend fun getFeeConfigs(): List<FeeConfig> {
        return feeConfigService.loadFeeConfigs()
    }

    @GetMapping("/{uuid}")
    suspend fun getUserFee(@PathVariable uuid: String): UserFee {
        return feeCalculator.getUserFee(uuid)
    }
}