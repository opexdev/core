package co.nilin.opex.wallet.app

import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.core.model.Wallet
import co.nilin.opex.wallet.core.model.WalletOwner
import co.nilin.opex.wallet.ports.postgres.dao.*
import co.nilin.opex.wallet.ports.postgres.impl.WalletManagerImpl
import co.nilin.opex.wallet.ports.postgres.model.CurrencyModel
import co.nilin.opex.wallet.ports.postgres.model.WalletModel
import co.nilin.opex.wallet.ports.postgres.model.WalletOwnerModel
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import reactor.core.publisher.Mono
import java.math.BigDecimal

private class WalletManagerTest {
    @Mock
    private var walletLimitsRepository: WalletLimitsRepository

    @Mock
    private var transactionRepository: TransactionRepository

    @Mock
    private var walletRepository: WalletRepository

    @Mock
    private var walletOwnerRepository: WalletOwnerRepository

    @Mock
    private var currencyRepository: CurrencyRepository

    private var walletManagerImpl: WalletManagerImpl

    private val walletOwner = object : WalletOwner {
        override fun id() = 2L
        override fun uuid() = "fdf453d7-0633-4ec7-852d-a18148c99a82"
        override fun title() = "wallet"
        override fun level() = "1"
        override fun isTradeAllowed() = true
        override fun isWithdrawAllowed() = true
        override fun isDepositAllowed() = true
    }

    private val currency = object : Currency {
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
        }
        transactionRepository = mock {
            on { calculateWithdrawStatistics(anyLong(), anyLong(), any(), any()) } doReturn Mono.empty()
        }
        walletOwnerRepository = mock {
            on { findById(walletOwner.id()) } doReturn Mono.just(
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
                findByOwnerAndTypeAndCurrency(walletOwner.id(), "main", currency.getSymbol())
            } doReturn Mono.just(
                WalletModel(
                    20L,
                    walletOwner.id(),
                    "main",
                    currency.getSymbol(),
                    BigDecimal.valueOf(1.2)
                )
            )
            on { save(any()) } doReturn Mono.just(
                WalletModel(
                    20L,
                    walletOwner.id(),
                    "main",
                    currency.getSymbol(),
                    BigDecimal.valueOf(1.2)
                )
            )
        }
        currencyRepository = mock {
            on { findBySymbol(currency.getSymbol()) } doReturn Mono.just(
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

    @Test
    fun givenFullWalletWithNoLimit_whenIsWithdrawAllowed_thenReturnTrue(): Unit = runBlocking {
        val wallet = object : Wallet {
            override fun id() = 20L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(0.5))
            override fun currency() = currency
            override fun type() = "main"
        }

        val isAllowed = walletManagerImpl.isWithdrawAllowed(wallet, BigDecimal.valueOf(0.5))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenEmptyWalletWithNoLimit_whenIsWithdrawAllowed_thenReturnFalse(): Unit = runBlocking {
        val wallet = object : Wallet {
            override fun id() = 20L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(0))
            override fun currency() = currency
            override fun type() = "main"
        }

        val isAllowed = walletManagerImpl.isWithdrawAllowed(wallet, BigDecimal.valueOf(0.5))

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenWalletOwner_whenFindWalletByOwnerAndCurrencyAndType_thenReturnWallet(): Unit = runBlocking {
        val wallet = walletManagerImpl.findWalletByOwnerAndCurrencyAndType(walletOwner, "main", currency)
        assertThat(wallet).isNotNull
        assertThat(wallet!!.owner().id()).isEqualTo(walletOwner.id())
        assertThat(wallet.currency().getSymbol()).isEqualTo(currency.getSymbol())
        assertThat(wallet.type()).isEqualTo("main")
    }

    @Test
    fun givenEmptyWalletWithNoLimit_whenCreateWallet_thenReturnWallet(): Unit = runBlocking {
        val wallet = walletManagerImpl.createWallet(
            walletOwner,
            Amount(currency, BigDecimal.valueOf(1)),
            currency,
            "main"
        )
        assertThat(wallet).isNotNull
        assertThat(wallet.owner().id()).isEqualTo(walletOwner.id())
        assertThat(wallet.currency().getSymbol()).isEqualTo(currency.getSymbol())
        assertThat(wallet.type()).isEqualTo("main")
    }
}
