package co.nilin.opex.port.wallet.postgres.impl

import co.nilin.opex.port.wallet.postgres.dao.TransactionRepository
import co.nilin.opex.port.wallet.postgres.dao.UserLimitsRepository
import co.nilin.opex.port.wallet.postgres.dao.WalletConfigRepository
import co.nilin.opex.port.wallet.postgres.dao.WalletOwnerRepository
import co.nilin.opex.port.wallet.postgres.model.UserLimitsModel
import co.nilin.opex.port.wallet.postgres.model.WalletConfigModel
import co.nilin.opex.port.wallet.postgres.model.WalletOwnerModel
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.WalletOwner
import co.nilin.opex.wallet.core.spi.WalletOwnerManager
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrDefault
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class WalletOwnerManagerImpl(
    val limitsRepository: UserLimitsRepository,
    val transactionRepository: TransactionRepository,
    val walletConfigRepository: WalletConfigRepository,
    val walletOwnerRepository: WalletOwnerRepository
) : WalletOwnerManager {

    val logger = LoggerFactory.getLogger(WalletOwnerManager::class.java)

    override suspend fun isDepositAllowed(owner: WalletOwner, amount: Amount): Boolean {
        var evaluate: Boolean? = limitsRepository.findByOwnerAndAction(
            owner.id()!!,
            "deposit"
        ).map { limit ->
            evaluateLimit(limit, owner, true)
        }.onEmpty {
            emit(true)
        }
            .reduce { a, b ->
                a && b
            }
        if (evaluate == null) {
            evaluate = limitsRepository.findByLevelAndAction(
                owner.level(),
                "deposit"
            )
                .map { limit ->
                    evaluateLimit(limit, owner, true)
                }.onEmpty {
                    emit(true)
                }.reduce { a, b ->
                    a && b
                }
        }
        logger.info("isDepositAllowed: {} {}{} {}", owner.uuid(), amount.amount, amount.currency.getName(), evaluate)
        return evaluate
    }

    private suspend fun evaluateLimit(
        limit: UserLimitsModel?,
        owner: WalletOwner,
        deposit: Boolean
    ): Boolean {
        var evaluate = true
        if (limit != null) {
            val mainCurrency = walletConfigRepository.findAll()
                .map { t: WalletConfigModel -> t.mainCurrency }
                .awaitFirstOrDefault("BTC")
            if (limit.dailyCount != null || limit.dailyTotal != null) {
                val ts = if (deposit) {
                    transactionRepository.calculateDepositStatisticsBasedOnCurrency(
                        owner.id()!!, limit.walletType, LocalDateTime.now().minusDays(1)
                            .withHour(0).withMinute(0).withSecond(0), LocalDateTime.now(), mainCurrency
                    )
                } else {
                    transactionRepository.calculateWithdrawStatisticsBasedOnCurrency(
                        owner.id()!!, limit.walletType, LocalDateTime.now().minusDays(1)
                            .withHour(0).withMinute(0).withSecond(0), LocalDateTime.now(), mainCurrency
                    )
                }.awaitFirstOrNull()
                if (ts != null) {
                    evaluate = !((limit.dailyCount != null && ts.cnt!! >= limit.dailyCount)
                            || (limit.dailyTotal != null && ts.total!! >= limit.dailyTotal))
                }
            }
            if (evaluate) {
                if (limit.monthlyCount != null || limit.monthlyTotal != null) {
                    val ts = if (deposit) {
                        transactionRepository.calculateDepositStatisticsBasedOnCurrency(
                            owner.id()!!, limit.walletType, LocalDateTime.now().minusMonths(1).withDayOfMonth(1)
                                .withHour(0).withMinute(0).withSecond(0), LocalDateTime.now(), mainCurrency
                        )
                    } else {
                        transactionRepository.calculateWithdrawStatisticsBasedOnCurrency(
                            owner.id()!!, limit.walletType, LocalDateTime.now().minusMonths(1).withDayOfMonth(1)
                                .withHour(0).withMinute(0).withSecond(0), LocalDateTime.now(), mainCurrency
                        )
                    }.awaitFirstOrNull()
                    if (ts != null) {
                        evaluate = !((limit.monthlyCount != null && ts.cnt!! >= limit.monthlyCount)
                                || (limit.monthlyTotal != null && ts.total!! >= limit.monthlyTotal))
                    }
                }
            }
        }
        return evaluate
    }


    override suspend fun isWithdrawAllowed(owner: WalletOwner, amount: Amount): Boolean {
        var evaluate: Boolean? = limitsRepository.findByOwnerAndAction(
            owner.id()!!,
            "withdraw"
        )
            .map { limit ->
                evaluateLimit(limit, owner, false)
            }.onEmpty {
                emit(true)
            }.reduce { a, b ->
                a && b
            }
        if (evaluate == null) {
            evaluate = limitsRepository.findByLevelAndAction(
                owner.level(),
                "withdraw"
            )
                .map { limit ->
                    evaluateLimit(limit, owner, false)
                }.onEmpty {
                    emit(true)
                }.reduce { a, b ->
                    a && b
                }
        }
        logger.info("isWithdrawAllowed: {} {}{} {}", owner.uuid(), amount.amount, amount.currency.getName(), evaluate)
        return evaluate
    }

    override suspend fun findWalletOwner(uuid: String): WalletOwner? {
        return walletOwnerRepository.findByUuid(uuid).awaitFirstOrNull()
    }

    override suspend fun createWalletOwner(uuid: String, title: String, userLevel: String): WalletOwner {
        return walletOwnerRepository.save(WalletOwnerModel(null, uuid, title, userLevel)).awaitFirst()
    }
}