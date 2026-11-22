package co.nilin.opex.wallet.app.service

import co.nilin.opex.wallet.app.controller.TransferController
import co.nilin.opex.wallet.app.listener.FinancialActionEventListenerImpl
import co.nilin.opex.wallet.ports.kafka.listener.model.FinancialActionEvent
import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.core.spi.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class TransferOperationCorrelationTest {
    @Test
    fun `transfer endpoint forwards execution reference and action reference to wallet command`() = runBlocking<Unit> {
        val manager = mockk<TransferManager>()
        val currencies = mockk<CurrencyServiceManager>()
        val wallets = mockk<WalletManager>()
        val owners = mockk<WalletOwnerManager>()
        val currency = CurrencyCommand("ETH", precision = BigDecimal("0.00000001"))
        val payer = WalletOwner(1L, "payer", "Payer", "registered", true, true, true)
        val recipient = payer.copy(id = 2L, uuid = "recipient")
        coEvery { currencies.fetchCurrency(any()) } returns currency
        coEvery { owners.findWalletOwner("payer") } returns payer
        coEvery { owners.findWalletOwner("recipient") } returns recipient
        coEvery { wallets.findWalletByOwnerAndCurrencyAndType(any(), any(), any()) } answers {
            Wallet(firstArg<WalletOwner>().id!!, firstArg(), Amount(currency, BigDecimal.TEN),
                currency, secondArg(), 0L)
        }
        val commands = mutableListOf<TransferCommand>()
        coEvery { manager.transfer(capture(commands)) } returns mockk(relaxed = true)
        val service = TransferService(manager, currencies, wallets, owners, mockk(), mockk(), mockk(), mockk())
        val controller = TransferController(service)
        listOf(TransferCategory.TRADE, TransferCategory.FEE).forEach { category ->
            controller.transfer("ETH", WalletType.EXCHANGE, "payer", WalletType.MAIN, "recipient",
                BigDecimal.ONE, TransferController.TransferBody("settlement", "accountant:fiActions:$category",
                    category, "accountant-trade:execution-1"))
        }
        val kafkaEvent = FinancialActionEvent().apply {
            uuid = "fee-action"
            symbol = "ETH"
            amount = BigDecimal.ONE
            senderWalletType = WalletType.EXCHANGE
            sender = "payer"
            receiverWalletType = WalletType.MAIN
            receiver = "recipient"
            description = "settlement"
            transferCategory = TransferCategory.FEE
            transferRef = "accountant:fiActions:FEE"
            operationId = "accountant-trade:execution-1"
        }
        FinancialActionEventListenerImpl(service, mockk(relaxed = true)).onEvent(kafkaEvent, 0, 1, 0)
        assertThat(commands).hasSize(3)
        commands.forEach { command ->
            assertThat(command.operationId).isEqualTo("accountant-trade:execution-1")
            assertThat(command.transferRef).isEqualTo("accountant:fiActions:${command.transferCategory}")
        }
    }
}
