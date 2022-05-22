package co.nilin.opex.wallet.core.service

import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.core.model.WalletOwner
import co.nilin.opex.wallet.core.spi.TransactionManager
import co.nilin.opex.wallet.core.spi.WalletListener
import co.nilin.opex.wallet.core.spi.WalletManager
import co.nilin.opex.wallet.core.spi.WalletOwnerManager
import org.mockito.kotlin.mock

internal open class TransferServiceTestBase {
    protected var walletOwnerManager: WalletOwnerManager = mock()
    protected var walletManager: WalletManager = mock()
    protected var walletListener: WalletListener = mock()
    protected var transactionManager: TransactionManager = mock()
    protected var transferService: TransferService =
        TransferService(walletManager, walletListener, walletOwnerManager, transactionManager)

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
}
