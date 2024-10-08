package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.exc.ConcurrentBalanceChangException
import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.core.spi.WalletManager
import co.nilin.opex.wallet.ports.postgres.dao.*
import co.nilin.opex.wallet.ports.postgres.dto.toPlainObject
import co.nilin.opex.wallet.ports.postgres.model.WalletModel
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class WalletManagerImpl(
    val walletLimitsRepository: WalletLimitsRepository,
    val transactionRepository: TransactionRepository,
    val walletRepository: WalletRepository,
    val walletOwnerRepository: WalletOwnerRepository,
    val currencyRepository: CurrencyRepository,
    @Value("b58dc8b2-9c0f-11ee-8c90-0242ac120002") val adminUuid: String? = "",
    @Value("10000") val minimumBalance: BigDecimal? = BigDecimal(10000)
) : WalletManager {

    private val logger = LoggerFactory.getLogger(WalletManagerImpl::class.java)

    override suspend fun isDepositAllowed(wallet: Wallet, amount: BigDecimal): Boolean {
        var limit = walletLimitsRepository.findByOwnerAndCurrencyAndWalletAndAction(
            wallet.owner.id!!, wallet.currency.symbol, wallet.id!!, WalletLimitAction.DEPOSIT
        ).awaitFirstOrNull()
        if (limit == null) {
            limit = walletLimitsRepository.findByOwnerAndCurrencyAndActionAndWalletType(
                wallet.owner.id!!, wallet.currency.symbol, WalletLimitAction.DEPOSIT, wallet.type
            ).awaitFirstOrNull()
            if (limit == null) {
                limit = walletLimitsRepository.findByLevelAndCurrencyAndActionAndWalletType(
                    wallet.owner.level, wallet.currency.symbol, WalletLimitAction.DEPOSIT, wallet.type
                ).awaitFirstOrNull()
            }
        }
        var evaluate = true
        if (limit != null) {
            if (limit.dailyCount != null || limit.dailyTotal != null) {
                val ts = transactionRepository.calculateDepositStatistics(
                    wallet.owner.id!!, wallet.id!!, LocalDateTime.now().minusDays(1)
                        .withHour(0).withMinute(0).withSecond(0), LocalDateTime.now()
                ).awaitFirstOrNull()
                evaluate = if (ts != null) {
                    ((limit.dailyCount != null && ts.cnt!! >= limit.dailyCount!!)
                            || (limit.dailyTotal != null && ts.total!! >= limit.dailyTotal))
                } else {
                    limit.dailyTotal?.let { it >= amount } ?: true
                }
            }

            if (evaluate && (limit.monthlyCount != null || limit.monthlyTotal != null)) {
                val ts = transactionRepository.calculateDepositStatistics(
                    wallet.owner.id!!, wallet.id!!, LocalDateTime.now().minusMonths(1)
                        .withDayOfMonth(1)
                        .withHour(0).withMinute(0).withSecond(0), LocalDateTime.now()
                ).awaitFirstOrNull()
                evaluate = if (ts != null) {
                    ((limit.dailyCount != null && ts.cnt!! >= limit.dailyCount!!)
                            || (limit.dailyTotal != null && ts.total!! >= limit.dailyTotal))
                } else {
                    limit.dailyTotal?.let { it >= amount } ?: true
                }
            }
        }
        return evaluate
    }

    override suspend fun isWithdrawAllowed(wallet: Wallet, amount: BigDecimal): Boolean {
        if (amount < BigDecimal.ZERO)
            throw OpexError.InvalidAmount.exception()

        var evaluate = wallet.balance.amount >= amount
        if (!evaluate) return false

        var limit = walletLimitsRepository.findByOwnerAndCurrencyAndWalletAndAction(
            wallet.owner.id!!, wallet.currency.symbol, wallet.id!!, WalletLimitAction.WITHDRAW
        ).awaitFirstOrNull()
        if (limit == null) {
            limit = walletLimitsRepository.findByOwnerAndCurrencyAndActionAndWalletType(
                wallet.owner.id!!, wallet.currency.symbol, WalletLimitAction.WITHDRAW, wallet.type
            ).awaitFirstOrNull()
            if (limit == null) {
                limit = walletLimitsRepository.findByLevelAndCurrencyAndActionAndWalletType(
                    wallet.owner.level, wallet.currency.symbol, WalletLimitAction.WITHDRAW, wallet.type
                ).awaitFirstOrNull()
            }
        }

        if (limit != null) {
            if (limit.dailyCount != null || limit.dailyTotal != null) {
                val ts = transactionRepository.calculateWithdrawStatistics(
                    wallet.owner.id!!, wallet.id!!, LocalDateTime.now().minusDays(1)
                        .withHour(0).withMinute(0).withSecond(0), LocalDateTime.now()
                ).awaitFirstOrNull()
                if (ts != null) {
                    evaluate = (limit.dailyCount != null && ts.cnt!! >= limit.dailyCount!!)
                            || (limit.dailyTotal != null && ts.total!! >= limit.dailyTotal)
                }
            }

            if (evaluate && (limit.monthlyCount != null || limit.monthlyTotal != null)) {
                val ts = transactionRepository.calculateWithdrawStatistics(
                    wallet.owner.id!!, wallet.id!!, LocalDateTime.now().minusMonths(1)
                        .withDayOfMonth(1)
                        .withHour(0).withMinute(0).withSecond(0), LocalDateTime.now()
                ).awaitFirstOrNull()
                if (ts != null) {
                    evaluate = (limit.dailyCount != null && ts.cnt!! >= limit.dailyCount!!)
                            || (limit.dailyTotal != null && ts.total!! >= limit.dailyTotal)
                }
            }
        }
        return evaluate
    }

    override suspend fun increaseBalance(wallet: Wallet, amount: BigDecimal) {
        require(amount >= BigDecimal.ZERO)
        logger.info("Increase balance {}, {}, {}, {}", wallet.id, wallet.balance, amount, wallet.version)
        val updateCount = walletRepository.updateBalance(wallet.id!!, amount).awaitFirst()
        if (updateCount != 1) {
            throw ConcurrentBalanceChangException("Increase wallet balance failed")
        }
    }

    override suspend fun decreaseBalance(wallet: Wallet, amount: BigDecimal) {
        require(amount >= BigDecimal.ZERO)
        logger.info("Decrease balance {}, {}, {}, {}", wallet.id, wallet.balance, amount, wallet.version)
        val updateCount = walletRepository.updateBalance(wallet.id!!, -amount, wallet.version!!).awaitFirst()
        if (updateCount != 1) {
            throw ConcurrentBalanceChangException("Decrease wallet balance failed")
        }
    }

    override suspend fun findWalletByOwnerAndCurrencyAndType(
        owner: WalletOwner,
        walletType: WalletType,
        currency: Currency
    ): Wallet? {

        val walletModel = walletRepository.findByOwnerAndTypeAndCurrency(
            owner.id!!,
            walletType,
            currency.symbol
        ).awaitFirstOrNull() ?: return null

        val existingCurrency = currencyRepository.findBySymbol(walletModel.currency)?.awaitFirst()
        val walletOwner = walletOwnerRepository.findById(walletModel.owner).awaitFirst().toPlainObject()
        return Wallet(
            walletModel.id!!,
            walletOwner,
            Amount(existingCurrency!!.toPlainObject(), walletModel.balance),
            existingCurrency.toPlainObject(),
            walletModel.type,
            walletModel.version
        )
    }

    override suspend fun findWallet(ownerId: Long, currency: String, walletType: WalletType): BriefWallet? {
        val wallet = walletRepository.findByOwnerAndTypeAndCurrency(ownerId, walletType, currency)
            .awaitSingleOrNull() ?: return null
        return BriefWallet(wallet.id, wallet.owner, wallet.balance, wallet.currency, wallet.type)
    }

    override suspend fun findWalletsByOwnerAndType(owner: WalletOwner, walletType: WalletType): List<Wallet> {
        val ownerModel = walletOwnerRepository.findById(owner.id!!).awaitFirst()
        return walletRepository.findByOwnerAndType(owner.id!!, walletType)
            .collectList()
            .awaitSingle()
            .map {
                val currency = currencyRepository.findById(it.currency).awaitFirst()
                Wallet(
                    it.id!!,
                    ownerModel.toPlainObject(),
                    Amount(currency.toPlainObject(), it.balance),
                    currency.toPlainObject(),
                    it.type,
                    it.version
                )
            }
    }

    suspend fun addSystemAndAdminWalletForNewCurrency(symbol: String) {
        val adminWallet = walletOwnerRepository.findByUuid(adminUuid!!).awaitSingleOrNull()
            ?: throw OpexError.Error.exception()

        val items = listOf(
            WalletModel(1, WalletType.MAIN, symbol, minimumBalance!!),
            WalletModel(1, WalletType.EXCHANGE, symbol, BigDecimal.ZERO),
            WalletModel(adminWallet.id!!, WalletType.MAIN, symbol, minimumBalance!!),
            WalletModel(adminWallet.id!!, WalletType.EXCHANGE, symbol, BigDecimal.ZERO)
        )
        walletRepository.saveAll(items).collectList().awaitSingleOrNull()
    }

    override suspend fun findWalletsByOwner(owner: WalletOwner): List<Wallet> {
        val ownerModel = walletOwnerRepository.findById(owner.id!!).awaitFirst()
        return walletRepository.findByOwner(owner.id!!)
            .collectList()
            .awaitSingle()
            .map {
                val currency = currencyRepository.findById(it.currency).awaitFirst()
                Wallet(
                    it.id!!,
                    ownerModel.toPlainObject(),
                    Amount(currency.toPlainObject(), it.balance),
                    currency.toPlainObject(),
                    it.type,
                    it.version
                )
            }
    }

    override suspend fun findWalletByOwnerAndSymbol(owner: WalletOwner, symbol: String): List<Wallet> {
        val ownerModel = walletOwnerRepository.findById(owner.id!!).awaitFirst()
        return walletRepository.findByOwnerAndCurrency(owner.id!!, symbol)
            .collectList()
            .awaitSingle()
            .map {
                val currency = currencyRepository.findById(it.currency).awaitFirst()
                Wallet(
                    it.id!!,
                    ownerModel.toPlainObject(),
                    Amount(currency.toPlainObject(), it.balance),
                    currency.toPlainObject(),
                    it.type,
                    it.version
                )
            }
    }

    override suspend fun createWallet(
        owner: WalletOwner,
        balance: Amount,
        currency: Currency,
        type: WalletType
    ): Wallet {
        return walletRepository.save(WalletModel(owner.id!!, type, currency.symbol, balance.amount))
            .awaitFirst()
            .toPlainObject(owner, currency)
    }

    override suspend fun createCashoutWallet(owner: WalletOwner, currency: Currency): Wallet {
        return walletRepository.save(WalletModel(owner.id!!, WalletType.CASHOUT, currency.symbol, BigDecimal.ZERO))
            .awaitFirst()
            .toPlainObject(owner, currency)
    }

    override suspend fun findWalletById(walletId: Long): Wallet? {
        val walletModel = walletRepository.findById(walletId).awaitFirstOrNull() ?: return null
        val existingCurrency = currencyRepository.findById(walletModel.currency).awaitFirst()
        return Wallet(
            walletModel.id!!,
            walletOwnerRepository.findById(walletModel.owner).awaitFirst().toPlainObject(),
            Amount(existingCurrency.toPlainObject(), walletModel.balance),
            existingCurrency.toPlainObject(),
            walletModel.type,
            walletModel.version
        )
    }

    override suspend fun findAllWalletsBriefNotZero(ownerId: Long): List<BriefWallet> {
        return walletRepository.findAllAmountNotZero(ownerId)
            .collectList()
            .awaitFirstOrElse { emptyList() }
            .map {
                BriefWallet(
                    it.id,
                    it.owner,
                    it.balance,
                    it.currency,
                    it.type,
                )
            }
    }
}
