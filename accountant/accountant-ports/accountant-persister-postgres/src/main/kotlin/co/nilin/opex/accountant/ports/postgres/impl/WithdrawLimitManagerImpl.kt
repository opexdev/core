package co.nilin.opex.accountant.ports.postgres.impl

import co.nilin.opex.accountant.core.model.BaseCurrency
import co.nilin.opex.accountant.core.spi.CurrencyRatePersister
import co.nilin.opex.accountant.core.spi.WithdrawLimitManager
import co.nilin.opex.accountant.ports.postgres.dao.UserWithdrawVolumeRepository
import co.nilin.opex.accountant.ports.postgres.dao.WithdrawLimitConfigRepository
import co.nilin.opex.common.OpexError
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.ZoneOffset

@Component
class WithdrawLimitManagerImpl(
    private val userWithdrawVolumeRepository: UserWithdrawVolumeRepository,
    private val withdrawLimitConfigRepository: WithdrawLimitConfigRepository,
    private val currencyRatePersister: CurrencyRatePersister,
    @Value("\${app.zone-offset}") private val zoneOffsetString: String

) : WithdrawLimitManager {

    override suspend fun canWithdraw(uuid: String, userLevel: String, currency: String, amount: BigDecimal): Boolean {
        val usdtAmount = when (currency) {
            BaseCurrency.USDT.name -> amount
            BaseCurrency.IRT.name -> amount.divide(
                currencyRatePersister.getRate(BaseCurrency.USDT.name, BaseCurrency.IRT.name),
                10,
                RoundingMode.DOWN
            )

            else -> amount.multiply(currencyRatePersister.getRate(currency, BaseCurrency.USDT.name))
        }
        val withdrawLimitConfig = (withdrawLimitConfigRepository.findByUserLevel(userLevel).awaitFirstOrNull()
            ?: throw OpexError.WithdrawLimitConfigNotFound.exception())

        val userWithdrawVolume =
            userWithdrawVolumeRepository.findTotalValueByUserAndAndDateAfter(
                uuid, LocalDate.now()
                    .atStartOfDay()
                    .atOffset(ZoneOffset.of(zoneOffsetString))
                    .toLocalDate()
            )
                .awaitFirstOrNull()

        return ((userWithdrawVolume?.valueUSDT
            ?: BigDecimal.ZERO) + usdtAmount) < withdrawLimitConfig.dailyMaxAmount
    }
}