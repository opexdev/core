package co.nilin.opex.wallet.core.service

import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.service.sample.VALID
import co.nilin.opex.wallet.core.spi.*
import io.mockk.MockKException
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

private class TransferManagerImplTest {
    private val walletOwnerManager: WalletOwnerManager = mockk()
    private val walletManager: WalletManager = mockk()
    private val walletListener: WalletListener = mockk()
    private val transactionManager: TransactionManager = mockk()
    private val userTxManager: UserTransactionManager = mockk()
    private val transferManager: TransferManagerImpl =
        TransferManagerImpl(walletManager, walletListener, walletOwnerManager, transactionManager, userTxManager)

    private fun stubWalletListener() {
        coEvery {
            walletListener.onWithdraw(any(), any(), eq(VALID.TRANSFER_COMMAND.amount), any())
        } returns Unit
        coEvery {
            walletListener.onDeposit(any(), any(), eq(VALID.TRANSFER_COMMAND.amount), any(), any())
        } returns Unit
    }

    @Test
    fun givenWalletWithAllowedTransfer_whenTransfer_thenReturnTransferResultDetailed(): Unit = runBlocking {
        coEvery { walletOwnerManager.isWithdrawAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount)) } returns true
        coEvery { walletOwnerManager.isDepositAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount)) } returns true
        coEvery { walletManager.isWithdrawAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } returns true
        coEvery { walletManager.isDepositAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } returns true
        coEvery { walletManager.decreaseBalance(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } returns Unit
        coEvery { walletManager.increaseBalance(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } returns Unit
        coEvery { walletManager.findWalletById(VALID.SOURCE_WALLET.id!!) } returns VALID.SOURCE_WALLET.copy(
            balance = VALID.SOURCE_WALLET.balance.copy(
                amount = VALID.SOURCE_WALLET.balance.amount - VALID.TRANSFER_COMMAND.amount.amount
            )
        )
        coEvery { walletListener.onWithdraw(any(), any(), any(), any()) } returns Unit
        coEvery { walletListener.onDeposit(any(), any(), any(), any(), any()) } returns Unit
        coEvery { transactionManager.save(any()) } returns "1"

        val result = transferManager.transfer(VALID.TRANSFER_COMMAND).transferResult

        assertThat(result).isNotNull
        assertThat(result.sourceUuid).isEqualTo(VALID.SOURCE_WALLET_OWNER.uuid)
        assertThat(result.sourceWalletType).isEqualTo(VALID.SOURCE_WALLET.type)
        assertThat(result.sourceBalanceBeforeAction).isEqualTo(VALID.SOURCE_WALLET.balance)
        assertThat(result.sourceBalanceAfterAction).isEqualTo(
            Amount(
                VALID.CURRENCY,
                VALID.SOURCE_WALLET.balance.amount - VALID.TRANSFER_COMMAND.amount.amount
            )
        )
        assertThat(result.amount).isEqualTo(VALID.TRANSFER_COMMAND.amount)
        assertThat(result.destUuid).isEqualTo(VALID.DEST_WALLET_OWNER.uuid)
        assertThat(result.destWalletType).isEqualTo(VALID.DEST_WALLET.type)
        assertThat(result.receivedAmount).isEqualTo(VALID.TRANSFER_COMMAND.amount)
    }

    @Test
    fun givenWalletWithOwnerWithdrawNotAllowed_whenTransfer_thenThrow(): Unit = runBlocking {
        coEvery {
            walletOwnerManager.isWithdrawAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount))
        } returns false
        coEvery { walletOwnerManager.isDepositAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount)) } returns true
        coEvery { walletManager.isWithdrawAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } returns true
        coEvery { walletManager.isDepositAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } returns true
        coEvery { walletManager.decreaseBalance(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } returns Unit
        coEvery { walletManager.increaseBalance(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } returns Unit
        coEvery { walletManager.findWalletById(VALID.SOURCE_WALLET.id!!) } returns VALID.SOURCE_WALLET
        coEvery { transactionManager.save(any()) } returns "1"

        assertThatThrownBy {
            runBlocking {
                transferManager.transfer(VALID.TRANSFER_COMMAND)
            }
        }.isNotInstanceOf(MockKException::class.java)
    }

    @Test
    fun givenWalletWithWithdrawNotAllowed_whenTransfer_thenThrow(): Unit = runBlocking {
        coEvery { walletOwnerManager.isWithdrawAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount)) } returns true
        coEvery { walletOwnerManager.isDepositAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount)) } returns true
        coEvery { walletManager.isWithdrawAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } returns false
        coEvery { walletManager.isDepositAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } returns true
        coEvery { walletManager.decreaseBalance(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } returns Unit
        coEvery { walletManager.increaseBalance(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } returns Unit
        coEvery { walletManager.findWalletById(1L) } returns VALID.SOURCE_WALLET
        stubWalletListener()
        coEvery { transactionManager.save(any()) } returns "1"

        assertThatThrownBy {
            runBlocking {
                transferManager.transfer(VALID.TRANSFER_COMMAND)
            }
        }.isNotInstanceOf(MockKException::class.java)
    }

    @Test
    fun givenWalletWithOwnerDepositNotAllowed_whenTransfer_thenThrow(): Unit = runBlocking {
        coEvery { walletOwnerManager.isWithdrawAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount)) } returns true
        coEvery { walletOwnerManager.isDepositAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount)) } returns false
        coEvery { walletManager.isWithdrawAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } returns true
        coEvery { walletManager.isDepositAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } returns true
        coEvery { walletManager.decreaseBalance(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } returns Unit
        coEvery { walletManager.increaseBalance(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } returns Unit
        coEvery { walletManager.findWalletById(VALID.SOURCE_WALLET.id!!) } returns VALID.SOURCE_WALLET
        stubWalletListener()
        coEvery { transactionManager.save(any()) } returns "1"

        assertThatThrownBy {
            runBlocking {
                transferManager.transfer(VALID.TRANSFER_COMMAND)
            }
        }.isNotInstanceOf(MockKException::class.java)
    }

    @Test
    fun givenWalletWithDepositNotAllowed_whenTransfer_thenThrow(): Unit = runBlocking {
        coEvery { walletOwnerManager.isWithdrawAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount)) } returns true
        coEvery { walletOwnerManager.isDepositAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount)) } returns true
        coEvery { walletManager.isWithdrawAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } returns true
        coEvery { walletManager.isDepositAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } returns false
        coEvery { walletManager.decreaseBalance(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } returns Unit
        coEvery { walletManager.increaseBalance(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } returns Unit
        coEvery { walletManager.findWalletById(1L) } returns VALID.SOURCE_WALLET
        stubWalletListener()
        coEvery { transactionManager.save(any()) } returns "1"

        assertThatThrownBy {
            runBlocking {
                transferManager.transfer(VALID.TRANSFER_COMMAND)
            }
        }.isNotInstanceOf(MockKException::class.java)
    }

    @Test
    fun givenNoWallet_whenTransfer_thenThrow(): Unit = runBlocking {
        coEvery { walletOwnerManager.isWithdrawAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount)) } returns true
        coEvery { walletOwnerManager.isDepositAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount)) } returns true
        coEvery { walletManager.isWithdrawAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } returns true
        coEvery { walletManager.isDepositAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } returns true
        coEvery {
            walletManager.decreaseBalance(
                any(),
                eq(VALID.TRANSFER_COMMAND.amount.amount)
            )
        } throws IllegalStateException()
        coEvery {
            walletManager.increaseBalance(
                any(),
                eq(VALID.TRANSFER_COMMAND.amount.amount)
            )
        } throws IllegalStateException()
        coEvery { walletManager.findWalletById(VALID.SOURCE_WALLET.id!!) } returns null
        stubWalletListener()
        coEvery { transactionManager.save(any()) } returns "1"

        assertThatThrownBy {
            runBlocking {
                transferManager.transfer(VALID.TRANSFER_COMMAND)
            }
        }.isNotInstanceOf(MockKException::class.java)
    }
}
