package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.KafkaEnabledTest
import co.nilin.opex.wallet.app.dto.TransactionRequest
import co.nilin.opex.wallet.core.inout.TransferResult
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.TransactionWithDetailHistory
import co.nilin.opex.wallet.core.model.WalletType
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
class TransferControllerIT : KafkaEnabledTest() {
    
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
        }
    }

    @Test
    fun givenCategory_whenTransfer_thenCategoryMatches() {
        runBlocking {
            val t = System.currentTimeMillis()
            val sender = walletOwnerManager.createWalletOwner(UUID.randomUUID().toString(), "sender", "")
            val receiver = sender.uuid
            val srcCurrency = currencyService.getCurrency("ETH")!!
            walletManager.createWallet(sender, Amount(srcCurrency, BigDecimal.valueOf(100)), srcCurrency, WalletType.MAIN)

            val transfer = webClient.post().uri("/v2/transfer/1_ETH/from/${sender.uuid}_main/to/${receiver}_exchange").accept(MediaType.APPLICATION_JSON)
                .bodyValue(TransferController.TransferBody("desc", "ref", "NORMAL"))
                .exchange()
                .expectStatus().isOk
                .expectBody(TransferResult::class.java)
                .returnResult().responseBody!!
            Assertions.assertEquals(BigDecimal.ONE, transfer.amount.amount)
            Assertions.assertEquals("ETH", transfer.amount.currency.symbol)
            val receiverWallet = walletManager.findWalletByOwnerAndCurrencyAndType(
                walletOwnerManager.findWalletOwner(receiver)!!, WalletType.MAIN, srcCurrency
            )
            Assertions.assertEquals(BigDecimal.ONE, receiverWallet!!.balance.amount)
            val txList = webClient.post().uri("/transaction/$receiver").accept(MediaType.APPLICATION_JSON)
                .bodyValue(TransactionRequest("ETH", null, t, System.currentTimeMillis(), 1, 0, true))
                .exchange()
                .expectStatus().isOk
                .expectBodyList(TransactionWithDetailHistory::class.java)
                .returnResult().responseBody
            Assertions.assertEquals(1, txList!!.size)
            with(txList[0]) {
                Assertions.assertEquals("NORMAL", this.category)
                Assertions.assertEquals("ETH", this.currency)
                Assertions.assertEquals(WalletType.MAIN, this.srcWalletType)
                Assertions.assertEquals(WalletType.EXCHANGE, this.destWalletType)
                Assertions.assertEquals(sender.uuid, this.senderUuid)
                Assertions.assertEquals(receiverUuid, this.receiverUuid)
            }
        }
    }
}
