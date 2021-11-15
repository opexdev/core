package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.ports.postgres.dao.*
import co.nilin.opex.wallet.ports.postgres.dto.SavedWallet
import co.nilin.opex.wallet.ports.postgres.model.WalletModel
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.core.model.Wallet
import co.nilin.opex.wallet.core.model.WalletOwner
import co.nilin.opex.wallet.core.spi.WalletManager
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class WalletManagerImpl(
    val walletLimitsRepository: WalletLimitsRepository,
    val transactionRepository: TransactionRepository,
    val walletRepository: WalletRepository,
    val walletOwnerRepository: WalletOwnerRepository,
    val currencyRepository: CurrencyRepository
) : WalletManager {

    override suspend fun isDepositAllowed(wallet: Wallet, amount: BigDecimal): Boolean {
        var limit = walletLimitsRepository.findByOwnerAndCurrencyAndWalletAndAction(
            wallet.owner().id()!!, wallet.currency().getName(), wallet.id()!!, "deposit"
        ).awaitFirstOrNull()
        if (limit == null) {
            limit = walletLimitsRepository.findByOwnerAndCurrencyAndActionAndWalletType(
                wallet.owner().id()!!, wallet.currency().getName(), wallet.type(), "deposit"
            ).awaitFirstOrNull()
            if (limit == null) {
                limit = walletLimitsRepository.findByLevelAndCurrencyAndActionAndWalletType(
                    wallet.owner().level(), wallet.currency().getName(), wallet.type(), "deposit"
                ).awaitFirstOrNull()
            }
        }
        var evaluate = true
        if (limit != null) {
            if (limit.dailyCount != null || limit.dailyTotal != null) {
                val ts = transactionRepository.calculateDepositStatistics(
                    wallet.owner().id()!!, wallet.id()!!, LocalDateTime.now().minusDays(1)
                        .withHour(0).withMinute(0).withSecond(0), LocalDateTime.now()
                ).awaitFirstOrNull()
                if (ts != null) {
                    evaluate = (limit.dailyCount != null && ts.cnt!! >= limit.dailyCount!!)
                            || (limit.dailyTotal != null && ts.total!! >= limit.dailyTotal)
                }
            }

            if (evaluate && (limit.monthlyCount != null || limit.monthlyTotal != null)) {
                val ts = transactionRepository.calculateDepositStatistics(
                    wallet.owner().id()!!, wallet.id()!!, LocalDateTime.now().minusMonths(1)
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

    override suspend fun isWithdrawAllowed(wallet: Wallet, amount: BigDecimal): Boolean {
        var evaluate = wallet.balance().amount >= amount
        if (evaluate) {
            var limit = walletLimitsRepository.findByOwnerAndCurrencyAndWalletAndAction(
                wallet.owner().id()!!, wallet.currency().getName(), wallet.id()!!, "withdraw"
            ).awaitFirstOrNull()
            if (limit == null) {
                limit = walletLimitsRepository.findByOwnerAndCurrencyAndActionAndWalletType(
                    wallet.owner().id()!!, wallet.currency().getName(), wallet.type(), "withdraw"
                ).awaitFirstOrNull()
                if (limit == null) {
                    limit = walletLimitsRepository.findByLevelAndCurrencyAndActionAndWalletType(
                        wallet.owner().level(), wallet.currency().getName(), wallet.type(), "withdraw"
                    ).awaitFirstOrNull()
                }
            }

            if (limit != null) {
                if (limit.dailyCount != null || limit.dailyTotal != null) {
                    val ts = transactionRepository.calculateWithdrawStatistics(
                        wallet.owner().id()!!, wallet.id()!!, LocalDateTime.now().minusDays(1)
                            .withHour(0).withMinute(0).withSecond(0), LocalDateTime.now()
                    ).awaitFirstOrNull()
                    if (ts != null) {
                        evaluate = (limit.dailyCount != null && ts.cnt!! >= limit.dailyCount!!)
                                || (limit.dailyTotal != null && ts.total!! >= limit.dailyTotal)
                    }
                }

                if (evaluate && (limit.monthlyCount != null || limit.monthlyTotal != null)) {
                    val ts = transactionRepository.calculateWithdrawStatistics(
                        wallet.owner().id()!!, wallet.id()!!, LocalDateTime.now().minusMonths(1)
                            .withDayOfMonth(1)
                            .withHour(0).withMinute(0).withSecond(0), LocalDateTime.now()
                    ).awaitFirstOrNull()
                    if (ts != null) {
                        evaluate = (limit.dailyCount != null && ts.cnt!! >= limit.dailyCount!!)
                                || (limit.dailyTotal != null && ts.total!! >= limit.dailyTotal)
                    }
                }
            }
        }
        return evaluate
    }

    override suspend fun increaseBalance(wallet: Wallet, amount: BigDecimal) {
        walletRepository.updateBalance(wallet.id()!!, amount).awaitFirst()
    }

    override suspend fun decreaseBalance(wallet: Wallet, amount: BigDecimal) {
        walletRepository.updateBalance(wallet.id()!!, -amount).awaitFirst()
    }

    override suspend fun findWalletByOwnerAndCurrencyAndType(
        owner: WalletOwner,
        walletType: String,
        currency: Currency
    ): Wallet? {
        val walletModel = walletRepository.findByOwnerAndTypeAndCurrency(
            owner.id()!!,
            walletType,
            currency.getName()
        ).awaitFirstOrNull() ?: return null

        val existingCurrency = currencyRepository.findById(walletModel.currency).awaitFirst()
        return SavedWallet(
            walletModel.id!!,
            walletOwnerRepository.findById(walletModel.owner).awaitFirst(),
            Amount(existingCurrency, walletModel.balance),
            existingCurrency,
            walletModel.type
        )
    }

    override suspend fun findWalletsByOwnerAndType(owner: WalletOwner, walletType: String): List<Wallet> {
        val ownerModel = walletOwnerRepository.findById(owner.id()!!).awaitFirst()
        return walletRepository.findByOwnerAndType(owner.id()!!, walletType)
            .collectList()
            .awaitSingle()
            .map {
                val currency = currencyRepository.findById(it.currency).awaitFirst()
                SavedWallet(
                    it.id!!,
                    ownerModel,
                    Amount(currency, it.balance),
                    currency,
                    it.type
                )
            }
    }

    override suspend fun findWalletsByOwner(owner: WalletOwner): List<Wallet> {
        val ownerModel = walletOwnerRepository.findById(owner.id()!!).awaitFirst()
        return walletRepository.findByOwner(owner.id()!!)
            .collectList()
            .awaitSingle()
            .map {
                val currency = currencyRepository.findById(it.currency).awaitFirst()
                SavedWallet(
                    it.id!!,
                    ownerModel,
                    Amount(currency, it.balance),
                    currency,
                    it.type
                )
            }
    }

    override suspend fun createWallet(owner: WalletOwner, balance: Amount, currency: Currency, type: String): Wallet {
        val walletModel = walletRepository
            .save(WalletModel(null, owner.id()!!, type, currency.getName(), balance.amount))
            .awaitFirst()
        val wallet = SavedWallet(
            walletModel.id!!,
            owner,
            Amount(currency, walletModel.balance),
            currency,
            walletModel.type
        )
        return wallet

    }

    override suspend fun findWalletById(
        walletId: Long
    ): Wallet? {
        val walletModel = walletRepository.findById(walletId).awaitFirstOrNull()
        if (walletModel == null)
            return null
        val existingCurrency = currencyRepository.findById(walletModel.currency).awaitFirst()
        return SavedWallet(
            walletModel.id!!,
            walletOwnerRepository.findById(walletModel.owner).awaitFirst(),
            Amount(existingCurrency, walletModel.balance),
            existingCurrency,
            walletModel.type
        )
    }
}