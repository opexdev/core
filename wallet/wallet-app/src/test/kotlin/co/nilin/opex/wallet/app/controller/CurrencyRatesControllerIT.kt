package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.KafkaEnabledTest
import co.nilin.opex.wallet.app.dto.CurrencyExchangeRate
import co.nilin.opex.wallet.app.dto.CurrencyExchangeRatesResponse
import co.nilin.opex.wallet.app.dto.CurrencyPair
import co.nilin.opex.wallet.app.dto.SetCurrencyExchangeRateRequest
import co.nilin.opex.wallet.core.inout.CurrencyCommand
import co.nilin.opex.wallet.core.model.FetchCurrency
import co.nilin.opex.wallet.core.model.otc.ForbiddenPair
import co.nilin.opex.wallet.core.model.otc.ForbiddenPairs
import co.nilin.opex.wallet.core.model.otc.Rate
import co.nilin.opex.wallet.core.model.otc.Symbols
import co.nilin.opex.wallet.core.service.otc.RateService
import co.nilin.opex.wallet.core.spi.CurrencyServiceManager
import co.nilin.opex.wallet.ports.postgres.dao.WalletRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.math.BigDecimal


@AutoConfigureWebTestClient
class CurrencyRatesControllerIT : KafkaEnabledTest() {

    @Autowired
    private lateinit var webClient: WebTestClient

    @Autowired
    private lateinit var currencyService: CurrencyServiceManager


    @Autowired
    lateinit var rateService: RateService

    @Autowired
    lateinit var walletRepository: WalletRepository

    @BeforeEach
    fun setup() {
        runBlocking {
            val currencies = listOf("E", "B", "U", "Z")
            val systemCurrencies = currencyService.fetchCurrencs(FetchCurrency())?.currencies?.filter { c -> currencies.contains(c.name) }?.map { currency -> currency.name }
            val fpair = rateService.getForbiddenPairs()
            val rates = rateService.getRate()
            fpair.forbiddenPairs!!.forEach { p -> rateService.deleteForbiddenPair(p) }
            rates.rates!!.forEach { r -> rateService.deleteRate(r) }
            //TODO: after moving the wallet creation to otcservice we can remove these two lines
            val wallets = walletRepository.findAll().collectList().block()
            //todo
//            wallets?.map {w->currencyService.fetchCurrencies(FetchCurrency(id=w.currency))?.currencies?.first()?.name }?.filter { w -> currencies.contains(w) }?.forEach { w -> walletRepository.delete(w).block() }
            systemCurrencies?.filter { c -> true }?.forEach { c -> currencyService.deleteCurrencies(FetchCurrency(name = c)) }
            currencies.forEach { c -> addCurrency(c, BigDecimal.TEN) }
        }
    }

    private suspend fun addCurrency(c: String, precision: BigDecimal) {
        try {
            currencyService.deleteCurrencies(FetchCurrency(symbol = c))
        } catch (_: Exception) {

        }
        currencyService.createNewCurrency(CurrencyCommand(symbol = c, name = c, precision = precision))
    }

    @Test
    fun whenSetCurrencyExchangeRateIsOK_thenRetrieveRoute() {
        runBlocking {
            webClient.post().uri("/otc/rate").accept(MediaType.APPLICATION_JSON)
                    .bodyValue(SetCurrencyExchangeRateRequest("E", "U", BigDecimal.TEN))
                    .exchange()
                    .expectStatus().isOk

            val routes = webClient.get().uri("/otc/route").accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(CurrencyExchangeRatesResponse::class.java)
                    .returnResult()
                    .responseBody!!
            Assertions.assertEquals(CurrencyExchangeRatesResponse(listOf(CurrencyExchangeRate("E", "U", BigDecimal.TEN))), routes)
        }
    }

    @Test
    fun whenSetCurrencyExchangeRateIsOK_thenRetrieveRates() {
        runBlocking {
            webClient.post().uri("/otc/rate").accept(MediaType.APPLICATION_JSON)
                    .bodyValue(SetCurrencyExchangeRateRequest("E", "U", BigDecimal.TEN))
                    .exchange()
                    .expectStatus().isOk

            webClient.post().uri("/otc/rate").accept(MediaType.APPLICATION_JSON)
                    .bodyValue(SetCurrencyExchangeRateRequest("B", "U", BigDecimal.TEN))
                    .exchange()
                    .expectStatus().isOk

            val allRates = webClient.get().uri("/otc/rate").accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(CurrencyExchangeRatesResponse::class.java)
                    .returnResult()
                    .responseBody!!
            Assertions.assertEquals(
                    CurrencyExchangeRatesResponse(
                            listOf(
                                    CurrencyExchangeRate("E", "U", BigDecimal.TEN), CurrencyExchangeRate("B", "U", BigDecimal.TEN)
                            )
                    ), allRates
            )

            val rate = webClient.get().uri("/otc/rate/E/U").accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(Rate::class.java)
                    .returnResult()
                    .responseBody!!
            Assertions.assertEquals(Rate("E", "U", BigDecimal.TEN), rate)
        }
    }

    @Test
    fun givenRateExist_whenRemoveCurrencyExchangeRate_thenRouteRemoved() {
        runBlocking {
            webClient.post().uri("/otc/rate").accept(MediaType.APPLICATION_JSON)
                    .bodyValue(SetCurrencyExchangeRateRequest("E", "U", BigDecimal.TEN))
                    .exchange()
                    .expectStatus().isOk

            webClient.delete().uri("/otc/rate/E/U").accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk

            val routes = webClient.get().uri("/otc/route").accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(CurrencyExchangeRatesResponse::class.java)
                    .returnResult()
                    .responseBody!!
            Assertions.assertTrue(routes.rates.isEmpty())
        }
    }

    @Test
    fun whenSetForbiddenPairs_thenStored() {
        runBlocking {
            webClient.post().uri("/otc/forbidden-pairs").accept(MediaType.APPLICATION_JSON)
                    .bodyValue(CurrencyPair("E", "U"))
                    .exchange()
                    .expectStatus().isOk

            val forbiddenPairs = webClient.get().uri("/otc/forbidden-pairs").accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(ForbiddenPairs::class.java)
                    .returnResult()
                    .responseBody!!
            Assertions.assertEquals(ForbiddenPair("E", "U"), forbiddenPairs.forbiddenPairs?.get(0))
        }
    }


    @Test
    fun givenForbiddenPair_whenRemoveForbiddenPairs_thenRemoved() {
        runBlocking {
            webClient.post().uri("/otc/forbidden-pairs").accept(MediaType.APPLICATION_JSON)
                    .bodyValue(CurrencyPair("E", "U"))
                    .exchange()
                    .expectStatus().isOk

            webClient.delete().uri("/otc/forbidden-pairs/E/U").accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk

            val forbiddenPairs = webClient.get().uri("/otc/forbidden-pairs").accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(ForbiddenPairs::class.java)
                    .returnResult()
                    .responseBody!!
            Assertions.assertTrue(forbiddenPairs.forbiddenPairs!!.isEmpty())
        }
    }

    @Test
    fun whenSetTransitiveSymbols_thenStored() {
        runBlocking {
            webClient.post().uri("/otc/transitive-symbols").accept(MediaType.APPLICATION_JSON)
                    .bodyValue(Symbols(listOf("E", "U")))
                    .exchange()
                    .expectStatus().isOk

            webClient.get().uri("/otc/transitive-symbols").accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .jsonPath("symbols[0]")
                    .isEqualTo("E")
                    .jsonPath("symbols[1]")
                    .isEqualTo("U")
        }
    }

    @Test
    fun givenTransitiveSymbols_whenRemoveTransitiveSymbol_thenRemoved() {
        runBlocking {
            webClient.post().uri("/otc/transitive-symbols").accept(MediaType.APPLICATION_JSON)
                    .bodyValue(
                            Symbols(listOf("E", "U"))
                    )
                    .exchange()
                    .expectStatus().isOk

            webClient.delete().uri("/otc/transitive-symbols/E").accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk

            webClient.get().uri("/otc/transitive-symbols").accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .jsonPath("symbols[0]").isEqualTo("U")
        }
    }

}