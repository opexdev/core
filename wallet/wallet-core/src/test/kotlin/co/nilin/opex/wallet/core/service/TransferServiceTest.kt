package co.nilin.opex.wallet.core.service

import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.service.sample.VALID
import co.nilin.opex.wallet.core.spi.TransactionManager
import co.nilin.opex.wallet.core.spi.WalletListener
import co.nilin.opex.wallet.core.spi.WalletManager
import co.nilin.opex.wallet.core.spi.WalletOwnerManager
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.*

private class TransferServiceTest {
    private val walletOwnerManager: WalletOwnerManager = mock()
    private val walletManager: WalletManager = mock()
    private val walletListener: WalletListener = mock()
    private val transactionManager: TransactionManager = mock()
    private val transferService: TransferService =
        TransferService(walletManager, walletListener, walletOwnerManager, transactionManager)

    @Test
    fun givenWalletWithAllowedTransfer_whenTransfer_thenReturnTransferResultDetailed(): Unit = runBlocking {
        stubbing(walletOwnerManager) {
            onBlocking { isWithdrawAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount)) } doReturn true
            onBlocking { isDepositAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount)) } doReturn true
        }
        stubbing(walletManager) {
            onBlocking { isWithdrawAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } doReturn true
            onBlocking { isDepositAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } doReturn true
            onBlocking { decreaseBalance(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } doReturn Unit
            onBlocking { increaseBalance(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } doReturn Unit
            onBlocking { findWalletById(1) } doReturn VALID.SOURCE_WALLET.copy(
                balance = VALID.SOURCE_WALLET.balance.copy(
                    amount = VALID.SOURCE_WALLET.balance.amount - VALID.TRANSFER_COMMAND.amount.amount
                )
            )
        }
        stubbing(walletListener) {
            onBlocking { onWithdraw(any(), any(), any(), anyString(), any()) } doReturn Unit
            onBlocking { onDeposit(any(), any(), any(), any(), anyString(), any()) } doReturn Unit
        }
        stubbing(transactionManager) {
            onBlocking { save(any()) } doReturn "1"
        }

        val result = transferService.transfer(VALID.TRANSFER_COMMAND).transferResult

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
        stubbing(walletOwnerManager) {
            onBlocking {
                isWithdrawAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount))
            } doReturn false
            onBlocking { isDepositAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount)) } doReturn true
        }
        stubbing(walletManager) {
            onBlocking { isWithdrawAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } doReturn true
            onBlocking { isDepositAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } doReturn true
            onBlocking { decreaseBalance(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } doReturn Unit
            onBlocking { increaseBalance(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } doReturn Unit
            onBlocking { findWalletById(1L) } doReturn VALID.SOURCE_WALLET
        }
        stubbing(walletListener) {
            onBlocking {
                onWithdraw(
                    any(),
                    any(),
                    eq(VALID.TRANSFER_COMMAND.amount),
                    anyString(),
                    any()
                )
            } doReturn Unit
            onBlocking {
                onDeposit(
                    any(),
                    any(),
                    eq(VALID.TRANSFER_COMMAND.amount),
                    any(),
                    anyString(),
                    any()
                )
            } doReturn Unit
        }
        stubbing(transactionManager) {
            onBlocking { save(any()) } doReturn "1"
        }

        assertThatThrownBy { runBlocking { transferService.transfer(VALID.TRANSFER_COMMAND) } }.isNotInstanceOf(
            NullPointerException::class.java
        )
    }

    @Test
    fun givenWalletWithWithdrawNotAllowed_whenTransfer_thenThrow(): Unit = runBlocking {
        stubbing(walletOwnerManager) {
            onBlocking { isWithdrawAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount)) } doReturn true
            onBlocking { isDepositAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount)) } doReturn true
        }
        stubbing(walletManager) {
            onBlocking { isWithdrawAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } doReturn false
            onBlocking { isDepositAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } doReturn true
            onBlocking { decreaseBalance(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } doReturn Unit
            onBlocking { increaseBalance(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } doReturn Unit
            onBlocking { findWalletById(1L) } doReturn VALID.SOURCE_WALLET
        }
        stubbing(walletListener) {
            onBlocking {
                onWithdraw(
                    any(),
                    any(),
                    eq(VALID.TRANSFER_COMMAND.amount),
                    anyString(),
                    any()
                )
            } doReturn Unit
            onBlocking {
                onDeposit(
                    any(),
                    any(),
                    eq(VALID.TRANSFER_COMMAND.amount),
                    any(),
                    anyString(),
                    any()
                )
            } doReturn Unit
        }
        stubbing(transactionManager) {
            onBlocking { save(any()) } doReturn "1"
        }

        assertThatThrownBy { runBlocking { transferService.transfer(VALID.TRANSFER_COMMAND) } }.isNotInstanceOf(
            NullPointerException::class.java
        )
    }

    @Test
    fun givenWalletWithOwnerDepositNotAllowed_whenTransfer_thenThrow(): Unit = runBlocking {
        stubbing(walletOwnerManager) {
            onBlocking { isWithdrawAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount)) } doReturn true
            onBlocking { isDepositAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount)) } doReturn false
        }
        stubbing(walletManager) {
            onBlocking { isWithdrawAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } doReturn true
            onBlocking { isDepositAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } doReturn true
            onBlocking { decreaseBalance(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } doReturn Unit
            onBlocking { increaseBalance(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } doReturn Unit
            onBlocking { findWalletById(1L) } doReturn VALID.SOURCE_WALLET
        }
        stubbing(walletListener) {
            onBlocking {
                onWithdraw(
                    any(),
                    any(),
                    eq(VALID.TRANSFER_COMMAND.amount),
                    anyString(),
                    any()
                )
            } doReturn Unit
            onBlocking {
                onDeposit(
                    any(),
                    any(),
                    eq(VALID.TRANSFER_COMMAND.amount),
                    any(),
                    anyString(),
                    any()
                )
            } doReturn Unit
        }
        stubbing(transactionManager) {
            onBlocking { save(any()) } doReturn "1"
        }

        assertThatThrownBy { runBlocking { transferService.transfer(VALID.TRANSFER_COMMAND) } }.isNotInstanceOf(
            NullPointerException::class.java
        )
    }

    @Test
    fun givenWalletWithDepositNotAllowed_whenTransfer_thenThrow(): Unit = runBlocking {
        stubbing(walletOwnerManager) {
            onBlocking { isWithdrawAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount)) } doReturn true
            onBlocking { isDepositAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount)) } doReturn true
        }
        stubbing(walletManager) {
            onBlocking { isWithdrawAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } doReturn true
            onBlocking { isDepositAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } doReturn false
            onBlocking { decreaseBalance(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } doReturn Unit
            onBlocking { increaseBalance(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } doReturn Unit
            onBlocking { findWalletById(1L) } doReturn VALID.SOURCE_WALLET
        }
        stubbing(walletListener) {
            onBlocking {
                onWithdraw(
                    any(),
                    any(),
                    eq(VALID.TRANSFER_COMMAND.amount),
                    anyString(),
                    any()
                )
            } doReturn Unit
            onBlocking {
                onDeposit(
                    any(),
                    any(),
                    eq(VALID.TRANSFER_COMMAND.amount),
                    any(),
                    anyString(),
                    any()
                )
            } doReturn Unit
        }
        stubbing(transactionManager) {
            onBlocking { save(any()) } doReturn "1"
        }

        assertThatThrownBy { runBlocking { transferService.transfer(VALID.TRANSFER_COMMAND) } }.isNotInstanceOf(
            NullPointerException::class.java
        )
    }

    @Test
    fun givenNoWallet_whenTransfer_thenThrow(): Unit = runBlocking {
        stubbing(walletOwnerManager) {
            onBlocking { isWithdrawAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount)) } doReturn true
            onBlocking { isDepositAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount)) } doReturn true
        }
        stubbing(walletManager) {
            onBlocking { isWithdrawAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } doReturn true
            onBlocking { isDepositAllowed(any(), eq(VALID.TRANSFER_COMMAND.amount.amount)) } doReturn true
            onBlocking {
                decreaseBalance(
                    any(),
                    eq(VALID.TRANSFER_COMMAND.amount.amount)
                )
            } doThrow IllegalStateException()
            onBlocking {
                increaseBalance(
                    any(),
                    eq(VALID.TRANSFER_COMMAND.amount.amount)
                )
            } doThrow IllegalStateException()
            onBlocking { findWalletById(1L) } doReturn null
        }
        stubbing(walletListener) {
            onBlocking {
                onWithdraw(
                    any(),
                    any(),
                    eq(VALID.TRANSFER_COMMAND.amount),
                    anyString(),
                    any()
                )
            } doReturn Unit
            onBlocking {
                onDeposit(
                    any(),
                    any(),
                    eq(VALID.TRANSFER_COMMAND.amount),
                    any(),
                    anyString(),
                    any()
                )
            } doReturn Unit
        }
        stubbing(transactionManager) {
            onBlocking { save(any()) } doReturn "1"
        }

        assertThatThrownBy { runBlocking { transferService.transfer(VALID.TRANSFER_COMMAND) } }.isNotInstanceOf(
            NullPointerException::class.java
        )
    }
}
