package co.nilin.opex.wallet.app

import co.nilin.opex.wallet.app.MockitoHelper.anyObject
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.core.model.Wallet
import co.nilin.opex.wallet.core.model.WalletOwner
import co.nilin.opex.wallet.ports.postgres.dao.*
import co.nilin.opex.wallet.ports.postgres.impl.WalletManagerImpl
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import reactor.core.publisher.Mono
import java.math.BigDecimal

private class WalletManagerTest {
    @Mock
    private lateinit var walletLimitsRepository: WalletLimitsRepository

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @Mock
    private lateinit var walletRepository: WalletRepository

    @Mock
    private lateinit var walletOwnerRepository: WalletOwnerRepository

    @Mock
    private lateinit var currencyRepository: CurrencyRepository

    private var walletManagerImpl: WalletManagerImpl

    private val walletOwner = object : WalletOwner {
        override fun id() = 2L
        override fun uuid() = "fdf453d7-0633-4ec7-852d-a18148c99a82"
        override fun title() = "wallet"
        override fun level() = "1"
        override fun isTradeAllowed() = true;
        override fun isWithdrawAllowed() = true;
        override fun isDepositAllowed() = true;
    }

    private val currency = object : Currency {
        override fun getSymbol() = "ETH"
        override fun getName() = "Ethereum"
        override fun getPrecision() = 0.0001
    }

    init {
        MockitoAnnotations.openMocks(this)
        runBlocking {
            Mockito.`when`(
                walletLimitsRepository.findByOwnerAndCurrencyAndWalletAndAction(
                    ArgumentMatchers.anyLong(),
                    ArgumentMatchers.anyString(),
                    ArgumentMatchers.anyLong(),
                    ArgumentMatchers.anyString()
                )
            )
                .thenReturn(Mono.empty())
            Mockito.`when`(
                walletLimitsRepository.findByOwnerAndCurrencyAndActionAndWalletType(
                    ArgumentMatchers.anyLong(),
                    ArgumentMatchers.anyString(),
                    ArgumentMatchers.anyString(),
                    ArgumentMatchers.anyString()
                )
            )
                .thenReturn(Mono.empty())
            Mockito.`when`(
                walletLimitsRepository.findByLevelAndCurrencyAndActionAndWalletType(
                    ArgumentMatchers.anyString(),
                    ArgumentMatchers.anyString(),
                    ArgumentMatchers.anyString(),
                    ArgumentMatchers.anyString()
                )
            )
                .thenReturn(Mono.empty())
            Mockito.`when`(
                transactionRepository.calculateWithdrawStatistics(
                    ArgumentMatchers.anyLong(),
                    ArgumentMatchers.anyLong(),
                    anyObject(),
                    anyObject()
                )
            )
                .thenReturn(Mono.empty())
        }
        walletManagerImpl = WalletManagerImpl(
            walletLimitsRepository,
            transactionRepository,
            walletRepository,
            walletOwnerRepository,
            currencyRepository
        )
    }

    @Test
    fun givenFullWalletWithNoLimit_whenIsWithdrawAllowed_thenReturnTrue() {
        val wallet = object : Wallet {
            override fun id() = 20L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(0.5))
            override fun currency() = currency
            override fun type() = "main"
        }

        val isAllowed = runBlocking { walletManagerImpl.isWithdrawAllowed(wallet, BigDecimal.valueOf(0.5)) }

        Assertions.assertEquals(true, isAllowed)
    }

    @Test
    fun givenEmptyWalletWithNoLimit_whenIsWithdrawAllowed_thenReturnFalse() {
        val wallet = object : Wallet {
            override fun id() = 20L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(0))
            override fun currency() = currency
            override fun type() = "main"
        }

        val isAllowed = runBlocking { walletManagerImpl.isWithdrawAllowed(wallet, BigDecimal.valueOf(0.5)) }

        Assertions.assertEquals(false, isAllowed)
    }
}
