package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.core.model.WalletOwner
import co.nilin.opex.wallet.ports.postgres.dao.TransactionRepository
import co.nilin.opex.wallet.ports.postgres.dao.UserLimitsRepository
import co.nilin.opex.wallet.ports.postgres.dao.WalletConfigRepository
import co.nilin.opex.wallet.ports.postgres.dao.WalletOwnerRepository
import org.mockito.kotlin.mock

internal open class WalletOwnerManagerTestBase {
    protected var userLimitsRepository: UserLimitsRepository = mock()
    protected var transactionRepository: TransactionRepository = mock()
    protected var walletOwnerRepository: WalletOwnerRepository = mock()
    protected var walletConfigRepository: WalletConfigRepository = mock()
    protected var walletOwnerManagerImpl: WalletOwnerManagerImpl = WalletOwnerManagerImpl(
        userLimitsRepository, transactionRepository, walletConfigRepository, walletOwnerRepository
    )

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
}
