package co.nilin.opex.market.core.spi

import co.nilin.opex.market.core.inout.UserCurrencyVolume
import co.nilin.opex.market.core.inout.UserTotalVolumeValue
import java.time.LocalDate

interface UserTradeHandler {

    suspend fun getTotalVolume(uuid: String, date: LocalDate): UserTotalVolumeValue

    suspend fun getVolumeByCurrency(uuid: String, currency: String, date: LocalDate): UserCurrencyVolume
}