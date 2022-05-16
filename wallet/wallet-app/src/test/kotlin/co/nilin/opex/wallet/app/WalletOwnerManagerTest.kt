package co.nilin.opex.wallet.app

import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.core.model.WalletOwner
import co.nilin.opex.wallet.ports.postgres.dao.TransactionRepository
import co.nilin.opex.wallet.ports.postgres.dao.UserLimitsRepository
import co.nilin.opex.wallet.ports.postgres.dao.WalletConfigRepository
import co.nilin.opex.wallet.ports.postgres.dao.WalletOwnerRepository
import co.nilin.opex.wallet.ports.postgres.impl.WalletOwnerManagerImpl
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

private class WalletOwnerManagerTest {
    @Mock
    private lateinit var userLimitsRepository: UserLimitsRepository

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @Mock
    private lateinit var walletOwnerRepository: WalletOwnerRepository

    @Mock
    private lateinit var walletConfigRepository: WalletConfigRepository

    private var walletOwnerManagerImpl: WalletOwnerManagerImpl

    init {
        MockitoAnnotations.openMocks(this)
        walletOwnerManagerImpl = WalletOwnerManagerImpl(
            userLimitsRepository, transactionRepository, walletConfigRepository, walletOwnerRepository
        )
    }

    @Test
    fun givenUUID_whenFindWalletOwner_thenReturnWalletOwner(): Unit = runBlocking {}

    @Test
    fun givenOwnerInfo_whenCreateWalletOwner_thenReturnWalletOwner(): Unit = runBlocking {}
}
