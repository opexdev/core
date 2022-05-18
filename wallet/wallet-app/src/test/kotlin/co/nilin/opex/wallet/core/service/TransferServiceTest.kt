package co.nilin.opex.wallet.core.service

import co.nilin.opex.wallet.core.inout.TransferCommand
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.Wallet
import co.nilin.opex.wallet.core.model.WalletOwner
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.stubbing
import java.math.BigDecimal

private class TransferServiceTest : TransferServiceTestBase() {
    @Test
    fun givenTransferCommand_whenTransfer_thenReturnTransferResultDetailed(): Unit = runBlocking {
        stubbing(walletOwnerManager) {
            on { runBlocking { isWithdrawAllowed(any(), eq(Amount(currency, BigDecimal.valueOf(0.5)))) } } doReturn true
            on { runBlocking { isDepositAllowed(any(), eq(Amount(currency, BigDecimal.valueOf(0.5)))) } } doReturn true
        }
        stubbing(walletManager) {
            on { runBlocking { isWithdrawAllowed(any(), eq(BigDecimal.valueOf(0.5))) } } doReturn true
            on { runBlocking { isDepositAllowed(any(), eq(BigDecimal.valueOf(0.5))) } } doReturn true
            on { runBlocking { decreaseBalance(any(), eq(BigDecimal.valueOf(0.5))) } } doReturn Unit
            on { runBlocking { increaseBalance(any(), eq(BigDecimal.valueOf(0.5))) } } doReturn Unit
            on { runBlocking { findWalletById(20L) } } doReturn object : Wallet {
                override fun id() = 20L
                override fun owner() = walletOwner
                override fun balance() = Amount(currency, BigDecimal.valueOf(1))
                override fun currency() = currency
                override fun type() = "main"
            }
        }
        stubbing(walletListener) {
            on { runBlocking { onWithdraw(any(), any(), any(), anyString(), any()) } } doReturn Unit
            on { runBlocking { onDeposit(any(), any(), any(), any(), anyString(), any()) } } doReturn Unit
        }
        stubbing(transactionManager) {
            on { runBlocking { save(any()) } } doReturn "1"
        }
        val sourceWalletOwner = object : WalletOwner {
            override fun id() = 2L
            override fun uuid() = "fdf453d7-0633-4ec7-852d-a18148c99a82"
            override fun title() = "wallet"
            override fun level() = "1"
            override fun isTradeAllowed() = true
            override fun isWithdrawAllowed() = true
            override fun isDepositAllowed() = true
        }
        val sourceWallet = object : Wallet {
            override fun id() = 20L
            override fun owner() = sourceWalletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(1.5))
            override fun currency() = currency
            override fun type() = "main"
        }
        val destWalletOwner = object : WalletOwner {
            override fun id() = 3L
            override fun uuid() = "e1950578-ef22-44e4-89f5-0b78feb03e2a"
            override fun title() = "wallet"
            override fun level() = "1"
            override fun isTradeAllowed() = true
            override fun isWithdrawAllowed() = true
            override fun isDepositAllowed() = true
        }
        val destWallet = object : Wallet {
            override fun id() = 30L
            override fun owner() = destWalletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(2.5))
            override fun currency() = currency
            override fun type() = "main"
        }
        val transferCommand = TransferCommand(
            sourceWallet,
            destWallet,
            Amount(currency, BigDecimal.valueOf(0.5)),
            null,
            null,
            null
        )

        val result = transferService.transfer(transferCommand).transferResult

        assertThat(result).isNotNull
        assertThat(result.amount).isEqualTo(Amount(currency, BigDecimal.valueOf(0.5)))
        assertThat(result.sourceUuid).isEqualTo("fdf453d7-0633-4ec7-852d-a18148c99a82")
        assertThat(result.destUuid).isEqualTo("e1950578-ef22-44e4-89f5-0b78feb03e2a")
        assertThat(result.sourceWalletType).isEqualTo("main")
        assertThat(result.destWalletType).isEqualTo("main")
        assertThat(result.sourceBalanceBeforeAction).isEqualTo(Amount(currency, BigDecimal.valueOf(1.5)))
        assertThat(result.sourceBalanceAfterAction).isEqualTo(Amount(currency, BigDecimal.valueOf(1)))
    }

    @Test
    fun givenOwnerNotWithdrawAllowed_whenTransfer_thenThrow(): Unit = runBlocking {
        stubbing(walletOwnerManager) {
            on {
                runBlocking {
                    isWithdrawAllowed(
                        any(),
                        eq(Amount(currency, BigDecimal.valueOf(0.5)))
                    )
                }
            } doReturn false
            on { runBlocking { isDepositAllowed(any(), eq(Amount(currency, BigDecimal.valueOf(0.5)))) } } doReturn true
        }
        stubbing(walletManager) {
            on { runBlocking { isWithdrawAllowed(any(), eq(BigDecimal.valueOf(0.5))) } } doReturn true
            on { runBlocking { isDepositAllowed(any(), eq(BigDecimal.valueOf(0.5))) } } doReturn true
            on { runBlocking { decreaseBalance(any(), eq(BigDecimal.valueOf(0.5))) } } doReturn Unit
            on { runBlocking { increaseBalance(any(), eq(BigDecimal.valueOf(0.5))) } } doReturn Unit
            on { runBlocking { findWalletById(20L) } } doReturn object : Wallet {
                override fun id() = 20L
                override fun owner() = walletOwner
                override fun balance() = Amount(currency, BigDecimal.valueOf(1))
                override fun currency() = currency
                override fun type() = "main"
            }
        }
        stubbing(walletListener) {
            on {
                runBlocking {
                    onWithdraw(
                        any(),
                        any(),
                        eq(Amount(currency, BigDecimal.valueOf(0.5))),
                        anyString(),
                        any()
                    )
                }
            } doReturn Unit
            on {
                runBlocking {
                    onDeposit(
                        any(),
                        any(),
                        eq(Amount(currency, BigDecimal.valueOf(0.5))),
                        any(),
                        anyString(),
                        any()
                    )
                }
            } doReturn Unit
        }
        stubbing(transactionManager) {
            on { runBlocking { save(any()) } } doReturn "1"
        }
        val sourceWalletOwner = object : WalletOwner {
            override fun id() = 2L
            override fun uuid() = "fdf453d7-0633-4ec7-852d-a18148c99a82"
            override fun title() = "wallet"
            override fun level() = "1"
            override fun isTradeAllowed() = true
            override fun isWithdrawAllowed() = true
            override fun isDepositAllowed() = true
        }
        val sourceWallet = object : Wallet {
            override fun id() = 20L
            override fun owner() = sourceWalletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(1.5))
            override fun currency() = currency
            override fun type() = "main"
        }
        val destWalletOwner = object : WalletOwner {
            override fun id() = 3L
            override fun uuid() = "e1950578-ef22-44e4-89f5-0b78feb03e2a"
            override fun title() = "wallet"
            override fun level() = "1"
            override fun isTradeAllowed() = true
            override fun isWithdrawAllowed() = true
            override fun isDepositAllowed() = true
        }
        val destWallet = object : Wallet {
            override fun id() = 30L
            override fun owner() = destWalletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(2.5))
            override fun currency() = currency
            override fun type() = "main"
        }
        val transferCommand = TransferCommand(
            sourceWallet,
            destWallet,
            Amount(currency, BigDecimal.valueOf(0.5)),
            null,
            null,
            null
        )

        assertThatThrownBy { runBlocking { transferService.transfer(transferCommand) } }
    }

    @Test
    fun givenWalletNotWithdrawAllowed_whenTransfer_thenThrow(): Unit = runBlocking {
        stubbing(walletOwnerManager) {
            on { runBlocking { isWithdrawAllowed(any(), eq(Amount(currency, BigDecimal.valueOf(0.5)))) } } doReturn true
            on { runBlocking { isDepositAllowed(any(), eq(Amount(currency, BigDecimal.valueOf(0.5)))) } } doReturn true
        }
        stubbing(walletManager) {
            on { runBlocking { isWithdrawAllowed(any(), eq(BigDecimal.valueOf(0.5))) } } doReturn false
            on { runBlocking { isDepositAllowed(any(), eq(BigDecimal.valueOf(0.5))) } } doReturn true
            on { runBlocking { decreaseBalance(any(), eq(BigDecimal.valueOf(0.5))) } } doReturn Unit
            on { runBlocking { increaseBalance(any(), eq(BigDecimal.valueOf(0.5))) } } doReturn Unit
            on { runBlocking { findWalletById(20L) } } doReturn object : Wallet {
                override fun id() = 20L
                override fun owner() = walletOwner
                override fun balance() = Amount(currency, BigDecimal.valueOf(1))
                override fun currency() = currency
                override fun type() = "main"
            }
        }
        stubbing(walletListener) {
            on {
                runBlocking {
                    onWithdraw(
                        any(),
                        any(),
                        eq(Amount(currency, BigDecimal.valueOf(0.5))),
                        anyString(),
                        any()
                    )
                }
            } doReturn Unit
            on {
                runBlocking {
                    onDeposit(
                        any(),
                        any(),
                        eq(Amount(currency, BigDecimal.valueOf(0.5))),
                        any(),
                        anyString(),
                        any()
                    )
                }
            } doReturn Unit
        }
        stubbing(transactionManager) {
            on { runBlocking { save(any()) } } doReturn "1"
        }
        val sourceWalletOwner = object : WalletOwner {
            override fun id() = 2L
            override fun uuid() = "fdf453d7-0633-4ec7-852d-a18148c99a82"
            override fun title() = "wallet"
            override fun level() = "1"
            override fun isTradeAllowed() = true
            override fun isWithdrawAllowed() = true
            override fun isDepositAllowed() = true
        }
        val sourceWallet = object : Wallet {
            override fun id() = 20L
            override fun owner() = sourceWalletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(1.5))
            override fun currency() = currency
            override fun type() = "main"
        }
        val destWalletOwner = object : WalletOwner {
            override fun id() = 3L
            override fun uuid() = "e1950578-ef22-44e4-89f5-0b78feb03e2a"
            override fun title() = "wallet"
            override fun level() = "1"
            override fun isTradeAllowed() = true
            override fun isWithdrawAllowed() = true
            override fun isDepositAllowed() = true
        }
        val destWallet = object : Wallet {
            override fun id() = 30L
            override fun owner() = destWalletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(2.5))
            override fun currency() = currency
            override fun type() = "main"
        }
        val transferCommand = TransferCommand(
            sourceWallet,
            destWallet,
            Amount(currency, BigDecimal.valueOf(0.5)),
            null,
            null,
            null
        )

        assertThatThrownBy { runBlocking { transferService.transfer(transferCommand) } }
    }

    @Test
    fun givenOwnerNotDepositAllowed_whenTransfer_thenThrow(): Unit = runBlocking {
        stubbing(walletOwnerManager) {
            on { runBlocking { isWithdrawAllowed(any(), eq(Amount(currency, BigDecimal.valueOf(0.5)))) } } doReturn true
            on { runBlocking { isDepositAllowed(any(), eq(Amount(currency, BigDecimal.valueOf(0.5)))) } } doReturn false
        }
        stubbing(walletManager) {
            on { runBlocking { isWithdrawAllowed(any(), eq(BigDecimal.valueOf(0.5))) } } doReturn true
            on { runBlocking { isDepositAllowed(any(), eq(BigDecimal.valueOf(0.5))) } } doReturn true
            on { runBlocking { decreaseBalance(any(), eq(BigDecimal.valueOf(0.5))) } } doReturn Unit
            on { runBlocking { increaseBalance(any(), eq(BigDecimal.valueOf(0.5))) } } doReturn Unit
            on { runBlocking { findWalletById(20L) } } doReturn object : Wallet {
                override fun id() = 20L
                override fun owner() = walletOwner
                override fun balance() = Amount(currency, BigDecimal.valueOf(1))
                override fun currency() = currency
                override fun type() = "main"
            }
        }
        stubbing(walletListener) {
            on {
                runBlocking {
                    onWithdraw(
                        any(),
                        any(),
                        eq(Amount(currency, BigDecimal.valueOf(0.5))),
                        anyString(),
                        any()
                    )
                }
            } doReturn Unit
            on {
                runBlocking {
                    onDeposit(
                        any(),
                        any(),
                        eq(Amount(currency, BigDecimal.valueOf(0.5))),
                        any(),
                        anyString(),
                        any()
                    )
                }
            } doReturn Unit
        }
        stubbing(transactionManager) {
            on { runBlocking { save(any()) } } doReturn "1"
        }
        val sourceWalletOwner = object : WalletOwner {
            override fun id() = 2L
            override fun uuid() = "fdf453d7-0633-4ec7-852d-a18148c99a82"
            override fun title() = "wallet"
            override fun level() = "1"
            override fun isTradeAllowed() = true
            override fun isWithdrawAllowed() = true
            override fun isDepositAllowed() = true
        }
        val sourceWallet = object : Wallet {
            override fun id() = 20L
            override fun owner() = sourceWalletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(1.5))
            override fun currency() = currency
            override fun type() = "main"
        }
        val destWalletOwner = object : WalletOwner {
            override fun id() = 3L
            override fun uuid() = "e1950578-ef22-44e4-89f5-0b78feb03e2a"
            override fun title() = "wallet"
            override fun level() = "1"
            override fun isTradeAllowed() = true
            override fun isWithdrawAllowed() = true
            override fun isDepositAllowed() = true
        }
        val destWallet = object : Wallet {
            override fun id() = 30L
            override fun owner() = destWalletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(2.5))
            override fun currency() = currency
            override fun type() = "main"
        }
        val transferCommand = TransferCommand(
            sourceWallet,
            destWallet,
            Amount(currency, BigDecimal.valueOf(0.5)),
            null,
            null,
            null
        )

        assertThatThrownBy { runBlocking { transferService.transfer(transferCommand) } }
    }

    @Test
    fun givenWalletNotDepositAllowed_whenTransfer_thenThrow(): Unit = runBlocking {
        stubbing(walletOwnerManager) {
            on { runBlocking { isWithdrawAllowed(any(), eq(Amount(currency, BigDecimal.valueOf(0.5)))) } } doReturn true
            on { runBlocking { isDepositAllowed(any(), eq(Amount(currency, BigDecimal.valueOf(0.5)))) } } doReturn true
        }
        stubbing(walletManager) {
            on { runBlocking { isWithdrawAllowed(any(), eq(BigDecimal.valueOf(0.5))) } } doReturn true
            on { runBlocking { isDepositAllowed(any(), eq(BigDecimal.valueOf(0.5))) } } doReturn false
            on { runBlocking { decreaseBalance(any(), eq(BigDecimal.valueOf(0.5))) } } doReturn Unit
            on { runBlocking { increaseBalance(any(), eq(BigDecimal.valueOf(0.5))) } } doReturn Unit
            on { runBlocking { findWalletById(20L) } } doReturn object : Wallet {
                override fun id() = 20L
                override fun owner() = walletOwner
                override fun balance() = Amount(currency, BigDecimal.valueOf(1))
                override fun currency() = currency
                override fun type() = "main"
            }
        }
        stubbing(walletListener) {
            on {
                runBlocking {
                    onWithdraw(
                        any(),
                        any(),
                        eq(Amount(currency, BigDecimal.valueOf(0.5))),
                        anyString(),
                        any()
                    )
                }
            } doReturn Unit
            on {
                runBlocking {
                    onDeposit(
                        any(),
                        any(),
                        eq(Amount(currency, BigDecimal.valueOf(0.5))),
                        any(),
                        anyString(),
                        any()
                    )
                }
            } doReturn Unit
        }
        stubbing(transactionManager) {
            on { runBlocking { save(any()) } } doReturn "1"
        }
        val sourceWalletOwner = object : WalletOwner {
            override fun id() = 2L
            override fun uuid() = "fdf453d7-0633-4ec7-852d-a18148c99a82"
            override fun title() = "wallet"
            override fun level() = "1"
            override fun isTradeAllowed() = true
            override fun isWithdrawAllowed() = true
            override fun isDepositAllowed() = true
        }
        val sourceWallet = object : Wallet {
            override fun id() = 20L
            override fun owner() = sourceWalletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(1.5))
            override fun currency() = currency
            override fun type() = "main"
        }
        val destWalletOwner = object : WalletOwner {
            override fun id() = 3L
            override fun uuid() = "e1950578-ef22-44e4-89f5-0b78feb03e2a"
            override fun title() = "wallet"
            override fun level() = "1"
            override fun isTradeAllowed() = true
            override fun isWithdrawAllowed() = true
            override fun isDepositAllowed() = true
        }
        val destWallet = object : Wallet {
            override fun id() = 30L
            override fun owner() = destWalletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(2.5))
            override fun currency() = currency
            override fun type() = "main"
        }
        val transferCommand = TransferCommand(
            sourceWallet,
            destWallet,
            Amount(currency, BigDecimal.valueOf(0.5)),
            null,
            null,
            null
        )

        assertThatThrownBy { runBlocking { transferService.transfer(transferCommand) } }
    }

    @Test
    fun givenNotExistWallet_whenTransfer_thenThrow(): Unit = runBlocking {
        stubbing(walletOwnerManager) {
            on { runBlocking { isWithdrawAllowed(any(), eq(Amount(currency, BigDecimal.valueOf(0.5)))) } } doReturn true
            on { runBlocking { isDepositAllowed(any(), eq(Amount(currency, BigDecimal.valueOf(0.5)))) } } doReturn true
        }
        stubbing(walletManager) {
            on { runBlocking { isWithdrawAllowed(any(), eq(BigDecimal.valueOf(0.5))) } } doReturn true
            on { runBlocking { isDepositAllowed(any(), eq(BigDecimal.valueOf(0.5))) } } doReturn true
            on { runBlocking { decreaseBalance(any(), eq(BigDecimal.valueOf(0.5))) } } doReturn Unit
            on { runBlocking { increaseBalance(any(), eq(BigDecimal.valueOf(0.5))) } } doReturn Unit
            on { runBlocking { findWalletById(20L) } } doReturn null
        }
        stubbing(walletListener) {
            on {
                runBlocking {
                    onWithdraw(
                        any(),
                        any(),
                        eq(Amount(currency, BigDecimal.valueOf(0.5))),
                        anyString(),
                        any()
                    )
                }
            } doReturn Unit
            on {
                runBlocking {
                    onDeposit(
                        any(),
                        any(),
                        eq(Amount(currency, BigDecimal.valueOf(0.5))),
                        any(),
                        anyString(),
                        any()
                    )
                }
            } doReturn Unit
        }
        stubbing(transactionManager) {
            on { runBlocking { save(any()) } } doReturn "1"
        }
        val sourceWalletOwner = object : WalletOwner {
            override fun id() = 2L
            override fun uuid() = "fdf453d7-0633-4ec7-852d-a18148c99a82"
            override fun title() = "wallet"
            override fun level() = "1"
            override fun isTradeAllowed() = true
            override fun isWithdrawAllowed() = true
            override fun isDepositAllowed() = true
        }
        val sourceWallet = object : Wallet {
            override fun id() = 20L
            override fun owner() = sourceWalletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(1.5))
            override fun currency() = currency
            override fun type() = "main"
        }
        val destWalletOwner = object : WalletOwner {
            override fun id() = 3L
            override fun uuid() = "e1950578-ef22-44e4-89f5-0b78feb03e2a"
            override fun title() = "wallet"
            override fun level() = "1"
            override fun isTradeAllowed() = true
            override fun isWithdrawAllowed() = true
            override fun isDepositAllowed() = true
        }
        val destWallet = object : Wallet {
            override fun id() = 30L
            override fun owner() = destWalletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(2.5))
            override fun currency() = currency
            override fun type() = "main"
        }
        val transferCommand = TransferCommand(
            sourceWallet,
            destWallet,
            Amount(currency, BigDecimal.valueOf(0.5)),
            null,
            null,
            null
        )

        assertThatThrownBy { runBlocking { transferService.transfer(transferCommand) } }
    }
}
