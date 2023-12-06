package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.TransactionRequest
import co.nilin.opex.wallet.core.inout.TransferResult
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.TransactionHistory
import co.nilin.opex.wallet.core.spi.CurrencyService
import co.nilin.opex.wallet.core.spi.WalletManager
import co.nilin.opex.wallet.core.spi.WalletOwnerManager
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import java.math.BigDecimal
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(TestChannelBinderConfiguration::class)
class TransferControllerIT {
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
            val sender = walletOwnerManager.findWalletOwner("1")!!
            val receiver = UUID.randomUUID().toString()
            val srcCurrency = currencyService.getCurrency("ETH")!!
            walletManager.createWallet(sender, Amount(srcCurrency, BigDecimal.valueOf(100)), srcCurrency, "main")

            val transfer = webClient.post().uri("/v2/transfer/1_ETH/from/1_main/to/${receiver}_main").accept(MediaType.APPLICATION_JSON)
                .bodyValue(TransferController.TransferBody("desc", "ref", "NORMAL", mapOf(Pair("key", "value"))))
                .exchange()
                .expectStatus().isOk
                .expectBody(TransferResult::class.java)
                .returnResult().responseBody!!
            Assertions.assertEquals(BigDecimal.ONE, transfer.amount.amount)
            Assertions.assertEquals("ETH", transfer.amount.currency.symbol)
            val txList = webClient.post().uri("/transaction/$receiver").accept(MediaType.APPLICATION_JSON)
                .bodyValue(TransactionRequest("ETH", null, t, System.currentTimeMillis(), 1, 1, true))
                .exchange()
                .expectStatus().isOk
                .expectBodyList(TransactionHistory::class.java)
                .returnResult().responseBody
            Assertions.assertEquals(1, txList!!.size)
            with(txList[0]) {
                Assertions.assertEquals("NORMAL", this.category)
                Assertions.assertEquals(mapOf(Pair("key", "value")), this.additionalData)
                Assertions.assertEquals("ETH", this.currency)
            }
        }
    }
}
