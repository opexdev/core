package co.nilin.opex.accountant.ports.postgres.impl

import co.nilin.opex.accountant.core.model.WithdrawLimitConfig
import co.nilin.opex.accountant.core.spi.CurrencyRatePersister
import co.nilin.opex.accountant.core.spi.WithdrawLimitManager
import co.nilin.opex.accountant.ports.postgres.dao.UserWithdrawVolumeRepository
import co.nilin.opex.accountant.ports.postgres.dao.WithdrawLimitConfigRepository
import co.nilin.opex.common.OpexError
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset

@Component
class WithdrawLimitManagerImpl(
    private val userWithdrawVolumeRepository: UserWithdrawVolumeRepository,
    private val withdrawLimitConfigRepository: WithdrawLimitConfigRepository,
    private val currencyRatePersister: CurrencyRatePersister,
    @Value("\${app.zone-offset}") private val zoneOffsetString: String,
    @Value("\${app.withdraw-volume-calculation-currency}") private val calculationCurrency: String

) : WithdrawLimitManager {

    override suspend fun canWithdraw(uuid: String, userLevel: String, currency: String, amount: BigDecimal): Boolean {

        val rate = if (currency == calculationCurrency) BigDecimal.ONE
        else currencyRatePersister.getRate(currency, calculationCurrency)

        val withdrawLimitConfig = (withdrawLimitConfigRepository.findByUserLevel(userLevel).awaitFirstOrNull()
            ?: throw OpexError.WithdrawLimitConfigNotFound.exception())

        val userWithdrawVolume = userWithdrawVolumeRepository.findTotalValueByUserAndAndDateAfter(
            uuid,
            LocalDateTime.now().atOffset(ZoneOffset.of(zoneOffsetString)).toLocalDate(),
            calculationCurrency
        ).awaitFirstOrNull()

        return ((userWithdrawVolume ?: BigDecimal.ZERO) + (amount.multiply(rate))) <= withdrawLimitConfig.dailyMaxAmount
    }

    override suspend fun getAll(): List<WithdrawLimitConfig> {
        return withdrawLimitConfigRepository.findAll()
            .map { model ->
                WithdrawLimitConfig(
                    name = model.name,
                    dailyMaxAmount = model.dailyMaxAmount
                )
            }
            .collectList()
            .awaitFirst()
    }
}