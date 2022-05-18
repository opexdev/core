package co.nilin.opex.wallet.core.service

import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.core.model.Wallet
import co.nilin.opex.wallet.core.model.WalletOwner
import co.nilin.opex.wallet.core.spi.TransactionManager
import co.nilin.opex.wallet.core.spi.WalletListener
import co.nilin.opex.wallet.core.spi.WalletManager
import co.nilin.opex.wallet.core.spi.WalletOwnerManager
import kotlinx.coroutines.runBlocking
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.math.BigDecimal

internal open class TransferServiceTestBase {
    @Mock
    protected var walletOwnerManager: WalletOwnerManager

    @Mock
    protected var walletManager: WalletManager

    @Mock
    protected var walletListener: WalletListener

    @Mock
    protected var transactionManager: TransactionManager

    protected var transferService: TransferService

    protected val currency = object : Currency {
        override fun getSymbol() = "ETH"
        override fun getName() = "Ethereum"
        override fun getPrecision() = 0.0001
    }

    protected val walletOwner = object : WalletOwner {
        override fun id() = 2L
        override fun uuid() = "fdf453d7-0633-4ec7-852d-a18148c99a82"
        override fun title() = "wallet"
        override fun level() = "1"
        override fun isTradeAllowed() = true
        override fun isWithdrawAllowed() = true
        override fun isDepositAllowed() = true
    }

    init {
        MockitoAnnotations.openMocks(this)
        walletOwnerManager = mock {
            on { runBlocking { isWithdrawAllowed(any(), any()) } } doReturn true
            on { runBlocking { isDepositAllowed(any(), any()) } } doReturn true
        }
        walletManager = mock {
            on { runBlocking { isWithdrawAllowed(any(), any()) } } doReturn true
            on { runBlocking { isDepositAllowed(any(), any()) } } doReturn true
            on { runBlocking { decreaseBalance(any(), any()) } } doReturn Unit
            on { runBlocking { increaseBalance(any(), any()) } } doReturn Unit
            on { runBlocking { findWalletById(20L) } } doReturn object : Wallet {
                override fun id() = 20L
                override fun owner() = walletOwner
                override fun balance() = Amount(currency, BigDecimal.valueOf(1))
                override fun currency() = currency
                override fun type() = "main"
            }
        }
        walletListener = mock {
            on { runBlocking { onWithdraw(any(), any(), any(), anyString(), any()) } } doReturn Unit
            on { runBlocking { onDeposit(any(), any(), any(), any(), anyString(), any()) } } doReturn Unit
        }
        transactionManager = mock {
            on { runBlocking { save(any()) } } doReturn "1"
        }
        transferService = TransferService(walletManager, walletListener, walletOwnerManager, transactionManager)
    }
}
