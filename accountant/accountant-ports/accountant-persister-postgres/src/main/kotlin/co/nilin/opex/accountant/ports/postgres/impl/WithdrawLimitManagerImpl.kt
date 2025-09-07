package co.nilin.opex.accountant.ports.postgres.impl

import co.nilin.opex.accountant.core.spi.WithdrawLimitManager
import co.nilin.opex.accountant.ports.postgres.dao.UserWithdrawVolumeRepository
import co.nilin.opex.accountant.ports.postgres.dao.WithdrawLimitConfigRepository
import co.nilin.opex.common.OpexError
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate

@Component
class WithdrawLimitManagerImpl(
    private val userWithdrawVolumeRepository: UserWithdrawVolumeRepository,
    private val withdrawLimitConfigRepository: WithdrawLimitConfigRepository
) : WithdrawLimitManager {

    override suspend fun canWithdraw(uuid: String, userLevel: String, currency: String, amount: BigDecimal): Boolean {
        val withdrawLimitConfig = (withdrawLimitConfigRepository.findByUserLevel(userLevel).awaitFirstOrNull()
            ?: throw OpexError.WithdrawLimitConfigNotFound.exception())
        val userWithdrawVolume =
            userWithdrawVolumeRepository.findTotalValueByUserAndAndDateAfter(uuid, LocalDate.now()).awaitFirstOrNull()
        return (userWithdrawVolume?.valueUSDT ?: BigDecimal.ZERO) < withdrawLimitConfig.dailyMaxAmount
    }
}