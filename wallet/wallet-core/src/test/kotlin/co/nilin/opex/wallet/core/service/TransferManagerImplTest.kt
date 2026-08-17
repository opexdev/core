package co.nilin.opex.wallet.core.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.Transaction
import co.nilin.opex.wallet.core.service.sample.VALID
import co.nilin.opex.wallet.core.spi.*
import io.mockk.MockKException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

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
        coEvery { transactionManager.save(any()) } returns 1

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
        coEvery { transactionManager.save(any()) } returns 1

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
        coEvery { transactionManager.save(any()) } returns 1

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
        coEvery { transactionManager.save(any()) } returns 1

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
        coEvery { transactionManager.save(any()) } returns 1

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
        coEvery { transactionManager.save(any()) } returns 1

        assertThatThrownBy {
            runBlocking {
                transferManager.transfer(VALID.TRANSFER_COMMAND)
            }
        }.isNotInstanceOf(MockKException::class.java)
    }

    @Test
    fun givenExistingTransferRef_whenTransfer_thenReturnIdempotentSuccessWithoutBalanceChanges(): Unit = runBlocking {
        val command = VALID.TRANSFER_COMMAND.copy(transferRef = "accountant:fiActions:abc")
        coEvery { transactionManager.findTransactionByTransferRef(eq(command.transferRef!!)) } returns co.nilin.opex.wallet.core.model.PersistedTransaction(
            100L,
            Transaction(
                VALID.SOURCE_WALLET,
                VALID.DEST_WALLET,
                command.amount.amount,
                command.destAmount.amount,
                command.description,
                command.transferRef,
                command.transferCategory,
                java.time.LocalDateTime.now()
            )
        )

        val result = transferManager.transfer(command)

        assertThat(result.tx).isEqualTo("100")
        assertThat(result.transferResult.sourceUuid).isEqualTo(command.sourceWallet.owner.uuid)
        assertThat(result.transferResult.destUuid).isEqualTo(command.destWallet.owner.uuid)

        coVerify(exactly = 0) { walletManager.decreaseBalance(any(), any()) }
        coVerify(exactly = 0) { walletManager.increaseBalance(any(), any()) }
        coVerify(exactly = 0) { transactionManager.save(any()) }
        coVerify(exactly = 0) { walletListener.onDeposit(any(), any(), any(), any(), any()) }
        coVerify(exactly = 0) { walletListener.onWithdraw(any(), any(), any(), any()) }
    }

    @Test
    fun givenExistingTransferRefWithDifferentParams_whenTransfer_thenThrowBadRequest(): Unit = runBlocking {
        val command = VALID.TRANSFER_COMMAND.copy(
            transferRef = "accountant:fiActions:abc",
            destWallet = VALID.DEST_WALLET.copy(id = 999L),
            amount = Amount(VALID.CURRENCY, BigDecimal("0.75")),
            destAmount = Amount(VALID.CURRENCY, BigDecimal("0.75"))
        )
        coEvery { transactionManager.findTransactionByTransferRef(eq(command.transferRef!!)) } returns co.nilin.opex.wallet.core.model.PersistedTransaction(
            100L,
            Transaction(
                VALID.SOURCE_WALLET,
                VALID.DEST_WALLET,
                VALID.TRANSFER_COMMAND.amount.amount,
                VALID.TRANSFER_COMMAND.destAmount.amount,
                VALID.TRANSFER_COMMAND.description,
                VALID.TRANSFER_COMMAND.transferRef,
                VALID.TRANSFER_COMMAND.transferCategory,
                java.time.LocalDateTime.now()
            )
        )

        val ex = Assertions.assertThrows(co.nilin.opex.utility.error.data.OpexException::class.java) {
            runBlocking {
                transferManager.transfer(command)
            }
        }

        assertThat(ex.error).isEqualTo(OpexError.BadRequest)
        coVerify(exactly = 0) { walletManager.decreaseBalance(any(), any()) }
        coVerify(exactly = 0) { walletManager.increaseBalance(any(), any()) }
        coVerify(exactly = 0) { transactionManager.save(any()) }
    }
}
