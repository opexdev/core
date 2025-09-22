package co.nilin.opex.accountant.app.controller

import co.nilin.opex.accountant.core.api.FeeCalculator
import co.nilin.opex.accountant.core.model.UserFee
import co.nilin.opex.accountant.core.spi.UserVolumePersister
import co.nilin.opex.common.utils.Interval
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/user/data")
class UserDataController(
    private val userVolumePersister: UserVolumePersister,
    private val feeCalculator: FeeCalculator,
    @Value("\${app.trade-volume-calculation-currency}")
    private val tradeVolumeCalculationCurrency: String,
) {
    @GetMapping("/{uuid}")
    suspend fun getUserFee(@PathVariable uuid: String): UserFee {
        return feeCalculator.getUserFee(uuid)
    }

    @GetMapping("/trade/volume/{uuid}")
    suspend fun getTradeVolumeByCurrency(
        @PathVariable uuid: String,
        @RequestParam symbol: String,
        @RequestParam interval: Interval
    ): BigDecimal {
        return userVolumePersister.getUserTotalTradeVolumeByCurrency(
            uuid,
            symbol,
            interval.getLocalDateTime().toLocalDate(),
            tradeVolumeCalculationCurrency
        ) ?: BigDecimal.ZERO
    }

    @GetMapping("/trade/volume/total/{uuid}")
    suspend fun getTotalTradeVolumeValue(
        @PathVariable uuid: String,
        @RequestParam interval: Interval
    ): BigDecimal {
        return userVolumePersister.getUserTotalTradeVolume(
            uuid,
            interval.getLocalDateTime().toLocalDate(),
            tradeVolumeCalculationCurrency
        ) ?: BigDecimal.ZERO
    }
}