package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.SetCurrencyExchangeRateRequest
import co.nilin.opex.wallet.app.dto.TransferPreEvaluateResponse
import co.nilin.opex.wallet.app.dto.TransferReserveRequest
import co.nilin.opex.wallet.app.dto.TransferReserveResponse
import co.nilin.opex.wallet.app.service.otc.CurrencyGraph
import co.nilin.opex.wallet.core.inout.TransferResult
import co.nilin.opex.wallet.core.model.Amount
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
class AdvancedTransferControllerIT {
    @Autowired
    private lateinit var webClient: WebTestClient

    @Autowired
    private lateinit var currencyGraph: CurrencyGraph

    @Autowired
    private lateinit var currencyService: CurrencyService

    @Autowired
    private lateinit var walletManager: WalletManager

    @Autowired
    private lateinit var walletOwnerManager: WalletOwnerManager

    @BeforeEach
    fun setup() {
        currencyGraph.reset()
        runBlocking {
            currencyService.addCurrency("ETH", "ETH", BigDecimal.TEN)
            currencyService.addCurrency("BTC", "BTC", BigDecimal.TEN)
            currencyService.addCurrency("USDT", "USDT", BigDecimal.valueOf(2))
        }

        webClient.post().uri("/rates").accept(MediaType.APPLICATION_JSON)
            .bodyValue(SetCurrencyExchangeRateRequest("ETH", "Z", BigDecimal.valueOf(100)))
            .exchange()
        webClient.post().uri("/rates").accept(MediaType.APPLICATION_JSON)
            .bodyValue(SetCurrencyExchangeRateRequest("BTC", "Z", BigDecimal.TEN))
            .exchange()
        webClient.post().uri("/rates").accept(MediaType.APPLICATION_JSON)
            .bodyValue(SetCurrencyExchangeRateRequest("Z", "USDT", BigDecimal.valueOf(2)))
            .exchange()
        webClient.post().uri("/transitive-symbols").accept(MediaType.APPLICATION_JSON)
            .bodyValue(listOf("Z"))
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
    fun whenReserveAndTransfer_thenTransferDone() {
        runBlocking {
            val sender = walletOwnerManager.findWalletOwner("1")!!
            val receiver = UUID.randomUUID().toString()
            val srcCurrency = currencyService.getCurrency("ETH")!!
            walletManager.createWallet(sender, Amount(srcCurrency, BigDecimal.valueOf(100)), srcCurrency, "main")

            val reserve = webClient.post().uri("/v3/transfer/reserve").accept(MediaType.APPLICATION_JSON)
                .bodyValue(TransferReserveRequest(BigDecimal.ONE, "ETH", "USDT", sender.uuid, "main", receiver, "main"))
                .exchange()
                .expectStatus().isOk
                .expectBody(TransferReserveResponse::class.java)
                .returnResult().responseBody!!
            val transfer = webClient.post().uri("/v3/transfer/${reserve.reserveUuid}?description=desc&transferRef=T1").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody(TransferResult::class.java)
                .returnResult().responseBody!!
            Assertions.assertEquals(reserve.guaranteedDestAmount, transfer.receivedAmount.amount)
            Assertions.assertEquals("USDT", transfer.receivedAmount.currency.symbol)
            Assertions.assertEquals(BigDecimal.ONE, transfer.amount.amount)
            Assertions.assertEquals("ETH", transfer.amount.currency.symbol)
        }
    }
}
