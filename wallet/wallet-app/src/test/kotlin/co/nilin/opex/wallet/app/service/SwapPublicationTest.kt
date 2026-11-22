package co.nilin.opex.wallet.app.service

import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.core.model.otc.ReservedTransfer
import co.nilin.opex.wallet.core.spi.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class SwapPublicationTest {
    @Test
    fun `publishes one complete conversion after both settlement legs`() = runBlocking<Unit> { scenario() }

    @Test
    fun `admin OTC preserves distinct recipient and quote and executor attribution`() = runBlocking<Unit> { scenario(admin = true) }

    @Test
    fun `does not commit or publish a conversion when its second leg fails`() = runBlocking<Unit> { scenario(failDestination = true) }

    private suspend fun scenario(admin: Boolean = false, failDestination: Boolean = false) {
        val transfers = mockk<TransferManager>()
        val currencies = mockk<CurrencyServiceManager>()
        val wallets = mockk<WalletManager>()
        val owners = mockk<WalletOwnerManager>()
        val reservations = mockk<ReservedTransferManager>(relaxed = true)
        val publisher = mockk<SwapEventSubmitter>(relaxed = true)
        val customer = WalletOwner(1L, "customer", "Customer", "registered", true, true, true)
        val system = customer.copy(id = 2L, uuid = "system")
        val usdt = CurrencyCommand("USDT", precision = BigDecimal("0.000001"))
        val irt = CurrencyCommand("IRT", precision = BigDecimal.ONE)
        val quotedAt = LocalDateTime.parse("2026-11-18T10:00:00")
        val recipient = if (admin) customer.copy(id = 3L, uuid = "recipient") else customer
        coEvery { owners.findWalletOwner("recipient") } returns recipient
        val reserve = ReservedTransfer(id = 7L, reserveNumber = "r-1", sourceSymbol = "USDT", destSymbol = "IRT",
            senderWalletType = WalletType.MAIN, senderUuid = customer.uuid,
            receiverWalletType = WalletType.MAIN, receiverUuid = recipient.uuid,
            sourceAmount = BigDecimal("10"), reservedDestAmount = BigDecimal("1500000"),
            rate = BigDecimal("150000"), reserveDate = quotedAt, expDate = quotedAt.plusMinutes(5),
            applicator = if (admin) "quote-admin" else null)
        coEvery { reservations.fetchValidReserve("r-1") } returns reserve
        every { owners.systemUuid } returns "system"
        coEvery { owners.findWalletOwner("customer") } returns customer
        coEvery { owners.findWalletOwner("system") } returns system
        coEvery { currencies.fetchCurrency(any()) } answers {
            if (firstArg<FetchCurrency>().symbol == "USDT") usdt else irt
        }
        coEvery { wallets.findWalletByOwnerAndCurrencyAndType(any(), any(), any()) } answers {
            val owner = firstArg<WalletOwner>()
            val currency = thirdArg<CurrencyCommand>()
            Wallet(owner.id!! * 10 + if (currency.symbol == "USDT") 1 else 2, owner,
                Amount(currency, BigDecimal("9999999")), currency, WalletType.MAIN, 0L)
        }
        val commands = mutableListOf<TransferCommand>()
        coEvery { transfers.transfer(capture(commands)) } answers {
            if (failDestination && commands.size == 2) throw IllegalStateException("destination failed")
            val command = firstArg<TransferCommand>()
            TransferResultDetailed(TransferResult(1L, command.sourceWallet.owner.uuid,
                command.sourceWallet.type, command.sourceWallet.balance, command.sourceWallet.balance,
                command.amount, command.destWallet.owner.uuid, command.destWallet.type, command.destAmount,
                command.sourceWallet.id, command.destWallet.id), if (commands.size == 1) "101" else "102")
        }
        val service = TransferService(transfers, currencies, wallets, owners,
            mockk(relaxed = true), reservations, mockk(relaxed = true), publisher)
        if (failDestination) {
            var failed = false
            try { service.advanceTransfer("r-1", null, "business-ref", customer.uuid) }
            catch (ex: IllegalStateException) { failed = true }
            assertThat(failed).isTrue()
            coVerify(exactly = 0) { reservations.commitReserve(any(), any()) }
            coVerify { publisher wasNot Called }
            return
        }
        val result = if (admin) service.advanceTransferByAdmin("r-1", "OTC", "business-ref", applicator = "execution-admin")
            else service.advanceTransfer("r-1", null, "business-ref", customer.uuid)
        assertThat(commands).hasSize(2)
        assertThat(commands.map { it.operationId }).containsOnly("swap:r-1")
        assertThat(commands.map { it.perspectivePartyId }).containsExactly("customer", recipient.uuid)
        assertThat(commands.map { it.amount.currency.symbol }).containsExactly("USDT", "IRT")
        assertThat(result.tx).isEqualTo("swap:r-1")
        coVerify(exactly = 1) { reservations.commitReserve("r-1", if (admin) "execution-admin" else null) }
        coVerify(exactly = 1) { publisher.send(
            uuid = "customer", txId = "swap:r-1", sourceAsset = "USDT", sourceAmount = BigDecimal("10"),
            destAsset = "IRT", destAmount = BigDecimal("1500000"), createDate = any(),
            sourceTransactionId = "101", destinationTransactionId = "102",
            sourceReference = "business-ref-r-1-withdraw", destinationReference = "business-ref-r-1-deposit",
            systemPartyId = "system", receiverPartyId = recipient.uuid,
            sourceSenderWalletId = "11", sourceReceiverWalletId = "21",
            destinationSenderWalletId = "22", destinationReceiverWalletId = if (admin) "32" else "12",
            sourceSenderWalletType = "MAIN", sourceReceiverWalletType = "MAIN",
            destinationSenderWalletType = "MAIN", destinationReceiverWalletType = "MAIN",
            reservationId = 7L, reservationReference = "r-1", quotedRate = BigDecimal("150000"),
            quotedAt = quotedAt, quoteExpiresAt = quotedAt.plusMinutes(5),
            transferCategory = if (admin) "IMPERSONATED_PURCHASE_FINALIZED" else "PURCHASE_FINALIZED",
            reservedBy = if (admin) "quote-admin" else null,
            executedBy = if (admin) "execution-admin" else null,
            description = if (admin) "OTC" else null, status = "COMPLETED"
        ) }
    }
}
