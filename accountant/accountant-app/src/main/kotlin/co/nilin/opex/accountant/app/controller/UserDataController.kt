package co.nilin.opex.accountant.app.controller

import co.nilin.opex.accountant.core.api.FeeCalculator
import co.nilin.opex.accountant.core.model.UserFee
import co.nilin.opex.accountant.core.spi.UserTradeVolumePersister
import co.nilin.opex.accountant.core.spi.UserWithdrawVolumePersister
import co.nilin.opex.common.utils.Interval
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDateTime

@RestController
@RequestMapping("/user/data")
class UserDataController(
    private val userVolumePersister: UserTradeVolumePersister,
    private val feeCalculator: FeeCalculator,
    private val userWithdrawVolumePersister: UserWithdrawVolumePersister,
    @Value("\${app.trade-volume-calculation-currency}")
    private val tradeVolumeCalculationCurrency: String,
) {
    @GetMapping("/fee/{uuid}")
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

    @GetMapping("/withdraw/volume/total/{uuid}")
    suspend fun getWithdrawVolumeValue(
        @PathVariable uuid: String,
        @RequestParam(required = false) interval: Interval?
    ): BigDecimal {
        return userWithdrawVolumePersister.getTotalValueByUserAndDateAfter(
            uuid,
            (interval?.getLocalDateTime() ?: LocalDateTime.now())
        )
    }

}