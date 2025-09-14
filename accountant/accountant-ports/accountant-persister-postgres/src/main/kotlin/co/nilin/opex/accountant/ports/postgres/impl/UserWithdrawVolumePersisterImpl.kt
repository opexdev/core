package co.nilin.opex.accountant.ports.postgres.impl

import co.nilin.opex.accountant.core.inout.UserTotalVolumeValue
import co.nilin.opex.accountant.core.model.BaseCurrency
import co.nilin.opex.accountant.core.model.WithdrawStatus
import co.nilin.opex.accountant.core.spi.CurrencyRatePersister
import co.nilin.opex.accountant.core.spi.UserWithdrawVolumePersister
import co.nilin.opex.accountant.ports.postgres.dao.UserWithdrawVolumeRepository
import co.nilin.opex.common.OpexError
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.ZoneOffset


@Component
class UserWithdrawVolumePersisterImpl(
    private val repository: UserWithdrawVolumeRepository,
    private val currencyRatePersister: CurrencyRatePersister,
    @Value("\${app.zone-offset}") private val zoneOffsetString: String
) : UserWithdrawVolumePersister {

    override suspend fun update(
        userId: String,
        currency: String,
        amount: BigDecimal,
        date: LocalDate,
        withdrawStatus: WithdrawStatus
    ) {
        val (usdtAmount, irtAmount) = convertAmount(currency, amount)
        val (adjustedUsdt, adjustedIrt) = adjustForWithdrawStatus(usdtAmount, irtAmount, withdrawStatus)

        repository.insertOrUpdate(
            userId,
            date.atStartOfDay().atOffset(ZoneOffset.of(zoneOffsetString)).toLocalDate(),
            adjustedUsdt,
            adjustedIrt
        ).awaitSingleOrNull()
    }

    override suspend fun getUserVolumeData(uuid: String, startDate: LocalDate): UserTotalVolumeValue {
        return repository.findTotalValueByUserAndAndDateAfter(uuid, startDate).awaitSingleOrNull()
            ?: UserTotalVolumeValue(BigDecimal.ZERO, BigDecimal.ZERO)
    }

    private suspend fun convertAmount(currency: String, amount: BigDecimal): Pair<BigDecimal, BigDecimal> {
        if (currency !in setOf(BaseCurrency.IRT.name, BaseCurrency.USDT.name) &&
            currencyRatePersister.getRate(currency, BaseCurrency.USDT.name) == BigDecimal.ZERO
        ) {
            throw OpexError.BadRequest.exception("Unsupported or invalid currency: $currency")
        }

        val usdtIrtRate = currencyRatePersister.getRate(BaseCurrency.USDT.name, BaseCurrency.IRT.name)
        if (usdtIrtRate == BigDecimal.ZERO) {
            throw OpexError.BadRequest.exception("Invalid USDT/IRT rate")
        } // FIXME خطا بدیم ؟ 

        return when (currency) {
            BaseCurrency.IRT.name -> {
                val irtAmount = amount
                val usdtAmount = amount.divide(usdtIrtRate, 10, RoundingMode.DOWN)
                usdtAmount to irtAmount
            }

            BaseCurrency.USDT.name -> {
                val usdtAmount = amount
                val irtAmount = amount.multiply(usdtIrtRate)
                usdtAmount to irtAmount
            }
            //FIXME تغییر نرخ چی میشه وقتی کنسل میشه ؟؟؟
            else -> {
                val usdtAmount = amount.multiply(currencyRatePersister.getRate(currency, BaseCurrency.USDT.name))
                val irtAmount = amount.multiply(currencyRatePersister.getRate(currency, BaseCurrency.IRT.name))
                usdtAmount to irtAmount
            }
        }
    }

    private fun adjustForWithdrawStatus(
        usdtAmount: BigDecimal,
        irtAmount: BigDecimal,
        withdrawStatus: WithdrawStatus
    ): Pair<BigDecimal, BigDecimal> {
        return if (withdrawStatus in listOf(WithdrawStatus.CANCELED, WithdrawStatus.REJECTED)) {
            usdtAmount.negate() to irtAmount.negate()
        } else {
            usdtAmount to irtAmount
        }
    }
}