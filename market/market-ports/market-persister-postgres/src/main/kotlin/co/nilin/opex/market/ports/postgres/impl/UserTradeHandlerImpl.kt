package co.nilin.opex.market.ports.postgres.impl

import co.nilin.opex.market.core.inout.UserCurrencyVolume
import co.nilin.opex.market.core.inout.UserTotalVolumeValue
import co.nilin.opex.market.core.spi.UserTradeHandler
import co.nilin.opex.market.ports.postgres.dao.UserTradeVolumeRepository
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class UserTradeHandlerImpl(private val repository: UserTradeVolumeRepository): UserTradeHandler {

    override suspend fun getTotalVolume(uuid: String, date: LocalDate): UserTotalVolumeValue {
        return repository.findTotalValueByUserAndAndDateAfter(uuid, date).awaitFirstOrNull()
            ?: UserTotalVolumeValue.zero()
    }

    override suspend fun getVolumeByCurrency(uuid: String, currency: String, date: LocalDate): UserCurrencyVolume {
        return repository.findByUserAndCurrencyAndDateAfter(uuid, currency, date).awaitFirstOrNull()
            ?: UserCurrencyVolume.zero(currency)
    }
}