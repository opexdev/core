package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.CurrencyExchangeRate
import co.nilin.opex.wallet.app.dto.CurrencyExchangeRatesResponse
import co.nilin.opex.wallet.app.dto.CurrencyPair
import co.nilin.opex.wallet.app.dto.SetCurrencyExchangeRateRequest
import co.nilin.opex.wallet.app.service.otc.CurrencyGraph
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

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient

@Import(TestChannelBinderConfiguration::class)
class CurrencyRatesControllerIT {
    @Autowired
    private lateinit var webClient: WebTestClient

    @Autowired
    private lateinit var currencyGraph: CurrencyGraph

    @BeforeEach
    fun setup() {
        currencyGraph.reset()
    }

    @Test
    fun whenSetCurrencyExchangeRateIsOK_thenRetrieveRoute() {
        runBlocking {
            webClient.post().uri("/rates").accept(MediaType.APPLICATION_JSON)
                .bodyValue(SetCurrencyExchangeRateRequest("ETH", "USDT", BigDecimal.TEN))
                .exchange()
                .expectStatus().isOk

            val routes = webClient.get().uri("/routes").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody(CurrencyExchangeRatesResponse::class.java)
                .returnResult()
                .responseBody!!
            Assertions.assertEquals(CurrencyExchangeRatesResponse(listOf(CurrencyExchangeRate("ETH", "USDT", BigDecimal.TEN))), routes)
        }
    }

    @Test
    fun whenSetCurrencyExchangeRateIsOK_thenRetrieveRates() {
        runBlocking {
            webClient.post().uri("/rates").accept(MediaType.APPLICATION_JSON)
                .bodyValue(SetCurrencyExchangeRateRequest("ETH", "USDT", BigDecimal.TEN))
                .exchange()
                .expectStatus().isOk

            webClient.post().uri("/rates").accept(MediaType.APPLICATION_JSON)
                .bodyValue(SetCurrencyExchangeRateRequest("BTC", "USDT", BigDecimal.TEN))
                .exchange()
                .expectStatus().isOk

            val allRates = webClient.get().uri("/rates/all/all").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody(CurrencyExchangeRatesResponse::class.java)
                .returnResult()
                .responseBody!!
            Assertions.assertEquals(
                CurrencyExchangeRatesResponse(
                    listOf(
                        CurrencyExchangeRate("ETH", "USDT", BigDecimal.TEN), CurrencyExchangeRate("BTC", "USDT", BigDecimal.TEN)
                    )
                ), allRates
            )

            val srcRates = webClient.get().uri("/rates/ETH/all").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody(CurrencyExchangeRatesResponse::class.java)
                .returnResult()
                .responseBody!!
            Assertions.assertEquals(CurrencyExchangeRatesResponse(listOf(CurrencyExchangeRate("ETH", "USDT", BigDecimal.TEN))), srcRates)

            val destRates = webClient.get().uri("/rates/ETH/USDT").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody(CurrencyExchangeRatesResponse::class.java)
                .returnResult()
                .responseBody!!
            Assertions.assertEquals(CurrencyExchangeRatesResponse(listOf(CurrencyExchangeRate("ETH", "USDT", BigDecimal.TEN))), destRates)
        }
    }

    @Test
    fun givenRateExist_whenRemoveCurrencyExchangeRate_thenRouteRemoved() {
        runBlocking {
            webClient.post().uri("/rates").accept(MediaType.APPLICATION_JSON)
                .bodyValue(SetCurrencyExchangeRateRequest("ETH", "USDT", BigDecimal.TEN))
                .exchange()
                .expectStatus().isOk

            webClient.delete().uri("/rates/ETH/USDT").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk

            val routes = webClient.get().uri("/routes").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody(CurrencyExchangeRatesResponse::class.java)
                .returnResult()
                .responseBody!!
            Assertions.assertTrue(routes.rates.isEmpty())
        }
    }

    ///rates/{sourceSymbol}/{destSymbol}

    @Test
    fun whenSetForbiddenPairs_thenStored() {
        runBlocking {
            webClient.post().uri("/forbidden-pairs").accept(MediaType.APPLICATION_JSON)
                .bodyValue(CurrencyPair("ETH", "USDT"))
                .exchange()
                .expectStatus().isOk

            val forbiddenPairs = webClient.get().uri("/forbidden-pairs").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBodyList(CurrencyPair::class.java)
                .returnResult()
                .responseBody!!
            Assertions.assertEquals(CurrencyPair("ETH", "USDT"), forbiddenPairs.get(0))
        }
    }


    @Test
    fun givenForbiddenPair_whenRemoveForbiddenPairs_thenRemoved() {
        runBlocking {
            webClient.post().uri("/forbidden-pairs").accept(MediaType.APPLICATION_JSON)
                .bodyValue(CurrencyPair("ETH", "USDT"))
                .exchange()
                .expectStatus().isOk

            webClient.delete().uri("/forbidden-pairs/ETH/USDT").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk

            val forbiddenPairs = webClient.get().uri("/forbidden-pairs").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBodyList(CurrencyPair::class.java)
                .returnResult()
                .responseBody!!
            Assertions.assertTrue(forbiddenPairs.isEmpty())
        }
    }

    @Test
    fun whenSetTransitiveSymbols_thenStored() {
        runBlocking {
            webClient.post().uri("/transitive-symbols").accept(MediaType.APPLICATION_JSON)
                .bodyValue(listOf("ETH", "USDT"))
                .exchange()
                .expectStatus().isOk

            webClient.get().uri("/transitive-symbols").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.[0]")
                .isEqualTo("ETH")
                .jsonPath("$.[1]")
                .isEqualTo("USDT")
        }
    }

    @Test
    fun givenTransitiveSymbols_whenRemoveTransitiveSymbol_thenRemoved() {
        runBlocking {
            webClient.post().uri("/transitive-symbols").accept(MediaType.APPLICATION_JSON)
                .bodyValue(listOf("ETH", "USDT"))
                .exchange()
                .expectStatus().isOk

            webClient.delete().uri("/transitive-symbols/ETH").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk

            webClient.get().uri("/transitive-symbols").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.[0]").isEqualTo("USDT")
        }
    }

}