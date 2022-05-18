package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.core.model.WalletOwner
import co.nilin.opex.wallet.ports.postgres.dao.*
import co.nilin.opex.wallet.ports.postgres.model.CurrencyModel
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import reactor.core.publisher.Mono

internal open class WalletManagerTestBase {
    protected var walletLimitsRepository: WalletLimitsRepository = mock()
    protected var transactionRepository: TransactionRepository = mock()
    protected var walletRepository: WalletRepository = mock()
    protected var walletOwnerRepository: WalletOwnerRepository = mock()
    protected var currencyRepository: CurrencyRepository = mock()
    protected var walletManagerImpl: WalletManagerImpl

    protected val walletOwner = object : WalletOwner {
        override fun id() = 2L
        override fun uuid() = "fdf453d7-0633-4ec7-852d-a18148c99a82"
        override fun title() = "wallet"
        override fun level() = "1"
        override fun isTradeAllowed() = true
        override fun isWithdrawAllowed() = true
        override fun isDepositAllowed() = true
    }

    protected val currency = object : Currency {
        override fun getSymbol() = "ETH"
        override fun getName() = "Ethereum"
        override fun getPrecision() = 0.0001
    }

    init {
        transactionRepository = mock {
            on { calculateWithdrawStatistics(anyLong(), anyLong(), any(), any()) } doReturn Mono.empty()
        }
        currencyRepository = mock {
            on { findBySymbol(currency.getSymbol()) } doReturn Mono.just(
                CurrencyModel(
                    currency.getSymbol(),
                    currency.getName(),
                    currency.getPrecision()
                )
            )
            on { findById(currency.getSymbol()) } doReturn Mono.just(
                CurrencyModel(
                    currency.getSymbol(),
                    currency.getName(),
                    currency.getPrecision()
                )
            )
        }
        walletManagerImpl = WalletManagerImpl(
            walletLimitsRepository, transactionRepository, walletRepository, walletOwnerRepository, currencyRepository
        )
    }
}
