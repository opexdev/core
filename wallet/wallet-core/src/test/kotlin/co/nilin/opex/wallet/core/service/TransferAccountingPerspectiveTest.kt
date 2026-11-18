package co.nilin.opex.wallet.core.service

import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.core.service.sample.VALID
import co.nilin.opex.wallet.core.spi.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TransferAccountingPerspectiveTest {
    @Test
    fun `classifies commissions and rewards separately and emits withdrawals only for completion transfers`() = runBlocking<Unit> {
        val wallets = mockk<WalletManager>(relaxed = true)
        val owners = mockk<WalletOwnerManager>(relaxed = true)
        val transactions = mockk<TransactionManager>(relaxed = true)
        val submitter = mockk<AccountingTransactionEventSubmitter>(relaxed = true)
        val events = mutableListOf<AccountingTransactionEvent>()
        coEvery { owners.isWithdrawAllowed(any(), any()) } returns true
        coEvery { owners.isDepositAllowed(any(), any()) } returns true
        coEvery { wallets.isWithdrawAllowed(any(), any()) } returns true
        coEvery { wallets.isDepositAllowed(any(), any()) } returns true
        coEvery { wallets.findWalletById(any()) } returns VALID.SOURCE_WALLET
        coEvery { wallets.findWallet(any(), any(), any()) } returns null
        coEvery { transactions.save(any()) } returns 42L
        coEvery { submitter.send(capture(events)) } returns Unit
        val manager = TransferManagerImpl(wallets, mockk(relaxed = true), owners, transactions,
            mockk(relaxed = true), submitter)
        val scenarios = listOf(
            TransferCategory.TRADE to "TRADE",
            TransferCategory.PURCHASE_FINALIZED to "SWAP",
            TransferCategory.IMPERSONATED_PURCHASE_FINALIZED to "SWAP",
            TransferCategory.REFERRAL_COMMISSION to "REFERRAL",
            TransferCategory.REFERENT_COMMISSION to "REFERRAL",
            TransferCategory.REFERRAL_KYC_REWARD to "REWARD",
            TransferCategory.KYC_ACCEPTED_REWARD to "REWARD",
            TransferCategory.DEPOSIT to "DEPOSIT",
            TransferCategory.DEPOSIT_MANUALLY to "DEPOSIT",
            TransferCategory.WITHDRAW_ACCEPT to "WITHDRAW",
            TransferCategory.WITHDRAW_MANUALLY to "WITHDRAW",
            TransferCategory.WITHDRAW_REQUEST to null,
            TransferCategory.WITHDRAW_CANCEL to null,
            TransferCategory.WITHDRAW_REJECT to null
        )
        scenarios.forEach { (category, expected) ->
            events.clear()
            manager.transfer(VALID.TRANSFER_COMMAND.copy(transferCategory = category))
            if (expected == null) {
                assertThat(events).describedAs(category.name).isEmpty()
            } else {
                assertThat(events).describedAs(category.name).hasSize(1)
                assertThat(events.single().type).isEqualTo(expected)
                assertThat(events.single().transferCategory).isEqualTo(category.name)
                if (expected == "REFERRAL" || expected == "REWARD") {
                    assertThat(events.single().userId).isEqualTo(VALID.DEST_WALLET_OWNER.uuid)
                    assertThat(events.single().side).isEqualTo("IN")
                }
            }
        }
    }

    @Test
    fun `incoming swap settlement belongs to recipient and preserves sender and receiver`() = runBlocking<Unit> {
        val wallets = mockk<WalletManager>(relaxed = true)
        val owners = mockk<WalletOwnerManager>(relaxed = true)
        val transactions = mockk<TransactionManager>(relaxed = true)
        val submitter = mockk<AccountingTransactionEventSubmitter>(relaxed = true)
        val emitted = slot<AccountingTransactionEvent>()
        coEvery { owners.isWithdrawAllowed(any(), any()) } returns true
        coEvery { owners.isDepositAllowed(any(), any()) } returns true
        coEvery { wallets.isWithdrawAllowed(any(), any()) } returns true
        coEvery { wallets.isDepositAllowed(any(), any()) } returns true
        coEvery { wallets.findWalletById(any()) } returns VALID.SOURCE_WALLET
        coEvery { transactions.findTransactionByTransferRef(any()) } returns null
        coEvery { transactions.save(any()) } returns 42L
        coEvery { submitter.send(capture(emitted)) } returns Unit
        val manager = TransferManagerImpl(wallets, mockk(relaxed = true), owners, transactions,
            mockk(relaxed = true), submitter)
        val command = VALID.TRANSFER_COMMAND.copy(
            transferCategory = TransferCategory.PURCHASE_FINALIZED,
            transferRef = "reservation-1-deposit", operationId = "swap:reservation-1",
            perspectivePartyId = VALID.DEST_WALLET_OWNER.uuid
        )
        manager.transfer(command)
        val event = emitted.captured
        assertThat(event.side).isEqualTo("IN")
        assertThat(event.userId).isEqualTo(VALID.DEST_WALLET_OWNER.uuid)
        assertThat(event.senderPartyId).isEqualTo(VALID.SOURCE_WALLET_OWNER.uuid)
        assertThat(event.receiverPartyId).isEqualTo(VALID.DEST_WALLET_OWNER.uuid)
        assertThat(event.senderWalletId).isEqualTo("1")
        assertThat(event.receiverWalletId).isEqualTo("2")
        assertThat(event.operationId).isEqualTo("swap:reservation-1")
        assertThat(event.walletTransactionRef).isEqualTo("42")
        assertThat(event.businessReference).isEqualTo("reservation-1-deposit")
        assertThat(event.recordRole).isEqualTo("SWAP_SETTLEMENT")
        assertThat(event.fee).isNull()
    }
}
