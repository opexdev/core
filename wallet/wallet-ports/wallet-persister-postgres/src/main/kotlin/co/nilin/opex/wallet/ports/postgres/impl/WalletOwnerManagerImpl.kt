package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.WalletOwner
import co.nilin.opex.wallet.core.spi.WalletOwnerManager
import co.nilin.opex.wallet.ports.postgres.dao.*
import co.nilin.opex.wallet.ports.postgres.dto.toPlainObject
import co.nilin.opex.wallet.ports.postgres.model.WalletConfigModel
import co.nilin.opex.wallet.ports.postgres.model.WalletLimitsModel
import co.nilin.opex.wallet.ports.postgres.model.WalletOwnerModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrDefault
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class WalletOwnerManagerImpl(
    val limitsRepository: WalletLimitsRepository,
    val transactionRepository: TransactionRepository,
    val walletConfigRepository: WalletConfigRepository,
    val walletOwnerRepository: WalletOwnerRepository
) : WalletOwnerManager {

    private val logger = LoggerFactory.getLogger(WalletOwnerManager::class.java)

    override suspend fun isDepositAllowed(owner: WalletOwner, amount: Amount): Boolean {
        require(amount.amount >= BigDecimal.ZERO)
        var evaluate = limitsRepository.findByOwnerAndAction(owner.id!!, "deposit")
            .map { limit -> evaluateLimit(amount.amount, limit, owner, true) }
            .onEmpty { emit(true) }
            .reduce { a, b -> a && b }

        if (evaluate) {
            evaluate = limitsRepository.findByLevelAndAction(owner.level, "deposit")
                .map { limit -> evaluateLimit(amount.amount, limit, owner, true) }
                .onEmpty { emit(true) }
                .reduce { a, b -> a && b }
        }

        logger.info("isDepositAllowed: {} {}{} {}", owner.uuid, amount.amount, amount.currency.name, evaluate)
        return evaluate
    }

    override suspend fun isWithdrawAllowed(owner: WalletOwner, amount: Amount): Boolean {
        require(amount.amount >= BigDecimal.ZERO)
        var evaluate = limitsRepository.findByOwnerAndAction(owner.id!!, "withdraw")
            .map { limit -> evaluateLimit(amount.amount, limit, owner, false) }
            .onEmpty { emit(true) }
            .reduce { a, b -> a && b }

        if (evaluate) {
            evaluate = limitsRepository.findByLevelAndAction(owner.level, "withdraw")
                .map { limit -> evaluateLimit(amount.amount, limit, owner, false) }
                .onEmpty { emit(true) }
                .reduce { a, b -> a && b }
        }

        logger.info("isWithdrawAllowed: {} {}{} {}", owner.uuid, amount.amount, amount.currency.name, evaluate)
        return evaluate
    }

    private suspend fun evaluateLimit(
        amount: BigDecimal,
        limit: WalletLimitsModel?,
        owner: WalletOwner,
        deposit: Boolean
    ): Boolean {
        if (limit == null)
            return true

        var evaluate = true
        val mainCurrency = walletConfigRepository.findAll()
            .map { it.mainCurrency }
            .awaitFirstOrDefault("BTC")

        if (limit.dailyCount != null || limit.dailyTotal != null) {
            val ts = if (deposit) {
                transactionRepository.calculateDepositStatisticsBasedOnCurrency(
                    owner.id!!, limit.walletType, LocalDateTime.now().minusDays(1)
                        .withHour(0).withMinute(0).withSecond(0), LocalDateTime.now(), mainCurrency
                )
            } else {
                transactionRepository.calculateWithdrawStatisticsBasedOnCurrency(
                    owner.id!!, limit.walletType, LocalDateTime.now().minusDays(1)
                        .withHour(0).withMinute(0).withSecond(0), LocalDateTime.now(), mainCurrency
                )
            }.awaitFirstOrNull()
            evaluate = if (ts != null) {
                !((limit.dailyCount != null && ts.cnt!! >= limit.dailyCount)
                        || (limit.dailyTotal != null && ts.total!! >= limit.dailyTotal))
            } else {
                limit.dailyTotal?.let { it >= amount } ?: true
            }
        }

        if (evaluate) {
            if (limit.monthlyCount != null || limit.monthlyTotal != null) {
                val ts = if (deposit) {
                    transactionRepository.calculateDepositStatisticsBasedOnCurrency(
                        owner.id!!, limit.walletType, LocalDateTime.now().minusMonths(1).withDayOfMonth(1)
                            .withHour(0).withMinute(0).withSecond(0), LocalDateTime.now(), mainCurrency
                    )
                } else {
                    transactionRepository.calculateWithdrawStatisticsBasedOnCurrency(
                        owner.id!!, limit.walletType, LocalDateTime.now().minusMonths(1).withDayOfMonth(1)
                            .withHour(0).withMinute(0).withSecond(0), LocalDateTime.now(), mainCurrency
                    )
                }.awaitFirstOrNull()
                evaluate = if (ts != null) {
                    !((limit.monthlyCount != null && ts.cnt!! >= limit.monthlyCount)
                            || (limit.monthlyTotal != null && ts.total!! >= limit.monthlyTotal))
                } else {
                    limit.monthlyTotal?.let { it >= amount } ?: true
                }
            }
        }
        return evaluate
    }

    override suspend fun findWalletOwner(uuid: String): WalletOwner? {
        return walletOwnerRepository.findByUuid(uuid).awaitFirstOrNull()?.toPlainObject()
    }

    override suspend fun createWalletOwner(uuid: String, title: String, userLevel: String): WalletOwner {
        return walletOwnerRepository.save(WalletOwnerModel(null, uuid, title, userLevel)).awaitFirst().toPlainObject()
    }
}
