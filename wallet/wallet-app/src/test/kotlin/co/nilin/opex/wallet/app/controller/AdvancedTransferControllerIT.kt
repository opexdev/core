package co.nilin.opex.wallet.app.controller

import co.nilin.opex.utility.error.controller.ExceptionController
import co.nilin.opex.wallet.app.KafkaEnabledTest
import co.nilin.opex.wallet.app.dto.*
import co.nilin.opex.wallet.core.inout.TransferResult
import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.core.model.otc.Symbols
import co.nilin.opex.wallet.core.spi.CurrencyService
import co.nilin.opex.wallet.core.spi.WalletManager
import co.nilin.opex.wallet.core.spi.WalletOwnerManager
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.math.BigDecimal
import java.util.*


@AutoConfigureWebTestClient
class AdvancedTransferControllerIT : KafkaEnabledTest() {
    @Autowired
    private lateinit var webClient: WebTestClient

    @Autowired
    private lateinit var currencyService: CurrencyService

    @Autowired
    private lateinit var walletManager: WalletManager

    @Autowired
    private lateinit var walletOwnerManager: WalletOwnerManager

    @BeforeEach
    fun setup() {

        runBlocking {
            currencyService.addCurrency("ETH", "ETH", BigDecimal.TEN)
            currencyService.addCurrency("BTC", "BTC", BigDecimal.TEN)
            currencyService.addCurrency("USDT", "USDT", BigDecimal.valueOf(2))
            currencyService.addCurrency("Z", "Z", BigDecimal.valueOf(2))
        }

        webClient.post().uri("/otc/rate").accept(MediaType.APPLICATION_JSON)
            .bodyValue(SetCurrencyExchangeRateRequest("ETH", "Z", BigDecimal.valueOf(100)))
            .exchange()
        webClient.post().uri("/otc/rate").accept(MediaType.APPLICATION_JSON)
            .bodyValue(SetCurrencyExchangeRateRequest("BTC", "Z", BigDecimal.TEN))
            .exchange()
        webClient.post().uri("/otc/rate").accept(MediaType.APPLICATION_JSON)
            .bodyValue(SetCurrencyExchangeRateRequest("Z", "USDT", BigDecimal.valueOf(2)))
            .exchange()
        webClient.post().uri("/otc/transitive-symbols").accept(MediaType.APPLICATION_JSON)
            .bodyValue(Symbols(listOf("Z")))
            .exchange()
            .expectStatus().isOk

    }

    @Test
    fun whenCalculateDestinationAmount_thenDestinationAmountMatch() {
        val evaluate = webClient.get().uri("/v3/amount/10_ETH/USDT").accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody(TransferPreEvaluateResponse::class.java)
            .returnResult().responseBody!!
        Assertions.assertEquals(BigDecimal.valueOf(2000), evaluate.destAmount)
    }

    @Test
    fun givenNotEnoughBalanceToSystem_whenReserve_thenException() {
        runBlocking {
            val sender = walletOwnerManager.createWalletOwner(UUID.randomUUID().toString(), "sender", "")
            val receiver = UUID.randomUUID().toString()
            val system = walletOwnerManager.findWalletOwner(walletOwnerManager.systemUuid)!!
            val srcCurrency = currencyService.getCurrency("ETH")!!
            val destCurrency = currencyService.getCurrency("USDT")!!
            createWalletWithCurrencyAndBalance(sender, WalletType.MAIN, srcCurrency, BigDecimal.valueOf(1))
            //not enough balance
            createWalletWithCurrencyAndBalance(system, WalletType.MAIN, destCurrency, BigDecimal.valueOf(1))

            webClient.post().uri("/v3/transfer/reserve").accept(MediaType.APPLICATION_JSON)
                .bodyValue(TransferReserveRequest(BigDecimal.ONE, "ETH", "USDT", sender.uuid, WalletType.MAIN, receiver, WalletType.MAIN))
                .exchange()
                .expectStatus().is5xxServerError
                .expectBody(ExceptionController.WebClientErrorResponse::class.java)

        }
    }

    @Test
    fun whenReserveAndTransfer_thenTransferDone() {
        runBlocking {
            val sender = walletOwnerManager.createWalletOwner(UUID.randomUUID().toString(), "sender", "")
            val receiver = UUID.randomUUID().toString()
            val system = walletOwnerManager.findWalletOwner(walletOwnerManager.systemUuid)!!
            val srcCurrency = currencyService.getCurrency("ETH")!!
            val destCurrency = currencyService.getCurrency("USDT")!!
            val senderInitWallet = createWalletWithCurrencyAndBalance(sender, WalletType.MAIN, srcCurrency, BigDecimal.valueOf(1))
            val systemDestCurrencyInitWallet = createWalletWithCurrencyAndBalance(system, WalletType.MAIN, destCurrency, BigDecimal.valueOf(200))
            val systemSrcCurrencyInitWallet = createWalletWithCurrencyAndBalance(system, WalletType.MAIN, srcCurrency, BigDecimal.valueOf(0))
            val reserve = webClient.post().uri("/v3/transfer/reserve").accept(MediaType.APPLICATION_JSON)
                .bodyValue(TransferReserveRequest(BigDecimal.ONE, "ETH", "USDT", sender.uuid, WalletType.MAIN, receiver, WalletType.MAIN))
                .exchange()
                .expectStatus().isOk
                .expectBody(ReservedTransferResponse::class.java)
                .returnResult().responseBody!!
            val transfer = webClient.post().uri("/v3/transfer/${reserve.reserveNumber}?description=desc&transferRef=T${UUID.randomUUID()}").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody(TransferResult::class.java)
                .returnResult().responseBody!!
            Assertions.assertEquals(reserve.guaranteedDestAmount, transfer.receivedAmount.amount)
            Assertions.assertEquals("USDT", transfer.receivedAmount.currency.symbol)
            Assertions.assertEquals(BigDecimal.ONE, transfer.amount.amount)
            Assertions.assertEquals("ETH", transfer.amount.currency.symbol)


            val senderWallet = walletManager.findWalletByOwnerAndCurrencyAndType(sender, WalletType.MAIN, srcCurrency)!!

            val systemWalletSrcCurrency = walletManager.findWalletByOwnerAndCurrencyAndType(system, WalletType.MAIN, srcCurrency)!!
            val systemWalletDestCurrency = walletManager.findWalletByOwnerAndCurrencyAndType(system, WalletType.MAIN, destCurrency)!!

            val receiverWallet = walletManager.findWalletByOwnerAndCurrencyAndType(walletOwnerManager.findWalletOwner(receiver)!!, WalletType.MAIN, destCurrency)!!

            Assertions.assertEquals(senderInitWallet.balance.amount - transfer.amount.amount, senderWallet.balance.amount)
            Assertions.assertEquals(systemSrcCurrencyInitWallet.balance.amount + transfer.amount.amount, systemWalletSrcCurrency.balance.amount)
            Assertions.assertEquals(systemDestCurrencyInitWallet.balance.amount - transfer.receivedAmount.amount, systemWalletDestCurrency.balance.amount)
            Assertions.assertEquals(transfer.receivedAmount.amount, receiverWallet.balance.amount)

        }
    }

    private suspend fun createWalletWithCurrencyAndBalance(system: WalletOwner, walletType: WalletType, destCurrency: Currency, balance: BigDecimal): Wallet {
        val wallet = walletManager.findWalletByOwnerAndCurrencyAndType(system, walletType, destCurrency)
        return if (wallet != null) {
            val amount = balance - wallet.balance.amount
            if (amount > BigDecimal.ZERO)
                walletManager.increaseBalance(wallet, amount)
            else if (amount < BigDecimal.ZERO)
                walletManager.decreaseBalance(wallet, -amount)
            walletManager.findWalletByOwnerAndCurrencyAndType(system, walletType, destCurrency)!!
        } else {
            walletManager.createWallet(system, Amount(destCurrency, balance), destCurrency, walletType)
        }
    }
}
