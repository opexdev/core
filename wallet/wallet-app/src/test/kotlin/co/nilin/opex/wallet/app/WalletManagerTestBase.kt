package co.nilin.opex.wallet.app

import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.core.model.WalletOwner
import co.nilin.opex.wallet.ports.postgres.dao.*
import co.nilin.opex.wallet.ports.postgres.impl.WalletManagerImpl
import co.nilin.opex.wallet.ports.postgres.model.CurrencyModel
import co.nilin.opex.wallet.ports.postgres.model.WalletLimitsModel
import co.nilin.opex.wallet.ports.postgres.model.WalletModel
import co.nilin.opex.wallet.ports.postgres.model.WalletOwnerModel
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import reactor.core.publisher.Mono
import java.math.BigDecimal

internal open class WalletManagerTestBase {
    @Mock
    protected var walletLimitsRepository: WalletLimitsRepository

    @Mock
    protected var transactionRepository: TransactionRepository

    @Mock
    protected var walletRepository: WalletRepository

    @Mock
    protected var walletOwnerRepository: WalletOwnerRepository

    @Mock
    protected var currencyRepository: CurrencyRepository

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
        MockitoAnnotations.openMocks(this)
        walletLimitsRepository = mock {
            on {
                findByOwnerAndCurrencyAndWalletAndAction(anyLong(), anyString(), anyLong(), anyString())
            } doReturn Mono.empty()
            on {
                findByOwnerAndCurrencyAndActionAndWalletType(anyLong(), anyString(), anyString(), anyString())
            } doReturn Mono.empty()
            on {
                findByLevelAndCurrencyAndActionAndWalletType(anyString(), anyString(), anyString(), anyString())
            } doReturn Mono.empty()
            on {
                findByOwnerAndCurrencyAndWalletAndAction(eq(2), eq("ETH"), eq(30), eq("withdraw"))
            } doReturn Mono.just(
                WalletLimitsModel(
                    1,
                    null,
                    2,
                    "withdraw",
                    "ETH",
                    "main",
                    30,
                    BigDecimal.valueOf(100),
                    10,
                    BigDecimal.valueOf(3000),
                    300
                )
            )
            on {
                findByOwnerAndCurrencyAndActionAndWalletType(eq(2), eq("ETH"), eq("withdraw"), eq("main"))
            } doReturn Mono.just(
                WalletLimitsModel(
                    1,
                    null,
                    2,
                    "withdraw",
                    "ETH",
                    "main",
                    30,
                    BigDecimal.valueOf(100),
                    10,
                    BigDecimal.valueOf(3000),
                    300
                )
            )
            on {
                findByLevelAndCurrencyAndActionAndWalletType(anyString(), eq("ETH"), eq("withdraw"), eq("main"))
            } doReturn Mono.just(
                WalletLimitsModel(
                    1,
                    null,
                    2,
                    "withdraw",
                    "ETH",
                    "main",
                    30,
                    BigDecimal.valueOf(100),
                    10,
                    BigDecimal.valueOf(3000),
                    300
                )
            )
        }
        transactionRepository = mock {
            on { calculateWithdrawStatistics(anyLong(), anyLong(), any(), any()) } doReturn Mono.empty()
        }
        walletOwnerRepository = mock {
            on { findById(walletOwner.id()!!) } doReturn Mono.just(
                WalletOwnerModel(
                    walletOwner.id(),
                    walletOwner.uuid(),
                    walletOwner.title(),
                    walletOwner.level(),
                    walletOwner.isTradeAllowed(),
                    walletOwner.isWithdrawAllowed(),
                    walletOwner.isDepositAllowed()
                )
            )
        }
        walletRepository = mock {
            on {
                findByOwnerAndTypeAndCurrency(walletOwner.id()!!, "main", currency.getSymbol())
            } doReturn Mono.just(
                WalletModel(
                    20L,
                    walletOwner.id()!!,
                    "main",
                    currency.getSymbol(),
                    BigDecimal.valueOf(1.2)
                )
            )
            on { save(any()) } doReturn Mono.just(
                WalletModel(
                    20L,
                    walletOwner.id()!!,
                    "main",
                    currency.getSymbol(),
                    BigDecimal.valueOf(1.2)
                )
            )
            on { findById(20) } doReturn Mono.just(
                WalletModel(
                    20L,
                    walletOwner.id()!!,
                    "main",
                    currency.getSymbol(),
                    BigDecimal.valueOf(0.5)
                )
            )
            on {
                updateBalance(any(), any())
            } doReturn Mono.just(0)
            on {
                updateBalance(eq(20), any())
            } doReturn Mono.just(1)
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
