package co.nilin.opex.wallet.app.service.otc

import co.nilin.opex.common.OpexError
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.wallet.app.KafkaEnabledTest
import co.nilin.opex.wallet.core.inout.CurrencyCommand
import co.nilin.opex.wallet.core.model.FetchCurrency
import co.nilin.opex.wallet.core.model.otc.ForbiddenPair
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
import java.math.BigDecimal


class CurrencyGraphIT : KafkaEnabledTest() {

    @Autowired
    lateinit var graphService: GraphService

    @Autowired
    lateinit var rateService: RateService

    @Autowired
    lateinit var currencyService: CurrencyServiceManager

    @Autowired
    lateinit var walletRepository: WalletRepository

    @BeforeEach
    fun setup() {
        runBlocking {
            val currencies = listOf("A", "B", "C", "D", "Z")
            val systemCurrencies = currencyService
                .fetchCurrencies(FetchCurrency())?.currencies?.filter { c -> currencies.contains(c.symbol) }?.map { currency -> currency.symbol }
            val fpair = rateService.getForbiddenPairs()
            val rates = rateService.getRate()
            fpair.forbiddenPairs!!.forEach { p -> rateService.deleteForbiddenPair(p) }
            rates.rates!!.forEach { r -> rateService.deleteRate(r) }
            //TODO: after moving the wallet creation to otcservice we can remove these two lines
            val wallets = walletRepository.findAll().collectList().block()
            wallets?.filter { w -> currencies.contains(w.currency.toString()) }?.forEach { w -> walletRepository.delete(w).block() }
            systemCurrencies?.filter { c -> true }?.forEach { c -> currencyService.deleteCurrency(FetchCurrency(name = c)) }
            currencies.forEach { c -> addCurrency(c) }
        }
    }

    @Test
    fun givenTwoPossibleRoute_whenAddCurrencyRates_thenShorterRouteIsAvailable() {
        runBlocking {
            rateService.addRate(Rate("A", "Z", BigDecimal.TEN))
            rateService.addRate(Rate("B", "Z", BigDecimal.TEN))
            rateService.addRate(Rate("Z", "C", BigDecimal.TEN))
            rateService.addRate(Rate("Z", "D", BigDecimal.TEN))
            rateService.addRate(Rate("A", "D", BigDecimal.TEN))

            val routes = graphService.getAvailableRoutes()
            Assertions.assertEquals(8, routes.size)
            val adRate = graphService.findRoute("A", "D")
            Assertions.assertEquals(adRate, Rate("A", "D", BigDecimal.TEN))
            val acRate = graphService.findRoute("A", "C")
            Assertions.assertEquals(acRate, Rate("A", "C", BigDecimal.valueOf(100)))
        }
    }

    @Test
    fun givenTwoPossibleRoute_whenRemoveCurrencyRate_thenLongerRouteIsAvailable() {
        runBlocking {
            rateService.addRate(Rate("A", "Z", BigDecimal.TEN))
            rateService.addRate(Rate("A", "D", BigDecimal.TEN))
            rateService.addRate(Rate("B", "Z", BigDecimal.TEN))
            rateService.addRate(Rate("Z", "C", BigDecimal.TEN))
            rateService.addRate(Rate("Z", "D", BigDecimal.TEN))
            rateService.deleteRate(Rate("A", "D", BigDecimal.TEN))

            val routes = graphService.getAvailableRoutes()
            Assertions.assertEquals(8, routes.size)
            val adRate = graphService.findRoute("A", "D")
            Assertions.assertEquals(adRate, Rate("A", "D", BigDecimal.valueOf(100)))
            val acRate = graphService.findRoute("A", "C")
            Assertions.assertEquals(acRate, Rate("A", "C", BigDecimal.valueOf(100)))
        }
    }

    @Test
    fun givenGraph_whenAddForbiddenRateNames_thenNoRouteIsAvailable() {
        runBlocking {
            rateService.addRate(Rate("A", "Z", BigDecimal.TEN))
            rateService.addRate(Rate("A", "D", BigDecimal.TEN))
            rateService.addRate(Rate("B", "Z", BigDecimal.TEN))
            rateService.addRate(Rate("Z", "C", BigDecimal.TEN))
            rateService.addRate(Rate("Z", "D", BigDecimal.TEN))
            rateService.addForbiddenPair(ForbiddenPair("A", "C"))

            val routes = graphService.getAvailableRoutes()
            Assertions.assertEquals(7, routes.size)
            val acRate = graphService.findRoute("A", "C")
            Assertions.assertNull(acRate)
        }
    }

    @Test
    fun givenGraph_whenAddForbiddenRateNames_thenExceptionAddingForbiddenRate() {
        runBlocking {
            rateService.addRate(Rate("A", "Z", BigDecimal.TEN))
            rateService.addRate(Rate("B", "Z", BigDecimal.TEN))
            rateService.addRate(Rate("Z", "C", BigDecimal.TEN))
            rateService.addRate(Rate("Z", "D", BigDecimal.TEN))
            rateService.addForbiddenPair(ForbiddenPair("A", "D"))
        }

        val exception = Assertions.assertThrows(OpexException::class.java) {
            runBlocking {
                rateService.addRate(Rate("A", "D", BigDecimal.TEN))
            }
        }
        Assertions.assertEquals(OpexError.ForbiddenPair, exception.error)
    }

    @Test
    fun givenGraphWithForbiddenRate_whenRemoveForbiddenRateNames_thenRouteIsAvailable() {
        runBlocking {
            rateService.addRate(Rate("A", "Z", BigDecimal.TEN))
            rateService.addRate(Rate("A", "D", BigDecimal.TEN))
            rateService.addRate(Rate("B", "Z", BigDecimal.TEN))
            rateService.addRate(Rate("Z", "C", BigDecimal.TEN))
            rateService.addRate(Rate("Z", "D", BigDecimal.TEN))
            rateService.addForbiddenPair(ForbiddenPair("A", "C"))

            Assertions.assertEquals(7, graphService.getAvailableRoutes().size)

            rateService.deleteForbiddenPair(ForbiddenPair("A", "C"))

            Assertions.assertEquals(8, graphService.getAvailableRoutes().size)

            val acRate = graphService.findRoute("A", "C")
            Assertions.assertEquals(acRate, Rate("A", "C", BigDecimal.valueOf(100)))

        }
    }

    @Test
    fun givenGraph_whenAddTransitiveSymbols_thenRoutesExcluded() {
        runBlocking {
            rateService.addRate(Rate("A", "Z", BigDecimal.TEN))
            rateService.addRate(Rate("A", "D", BigDecimal.TEN))
            rateService.addRate(Rate("B", "Z", BigDecimal.TEN))
            rateService.addRate(Rate("Z", "C", BigDecimal.TEN))
            rateService.addRate(Rate("Z", "D", BigDecimal.TEN))

            Assertions.assertEquals(8, graphService.getAvailableRoutes().size)

            rateService.addTransitiveSymbols(Symbols(listOf("Z")))

            Assertions.assertEquals(4, graphService.getAvailableRoutes().size)
        }
    }

    @Test
    fun givenGraphTransitiveSymbols_whenRemoveTransitiveSymbols_thenRouteIncluded() {
        runBlocking {
            rateService.addRate(Rate("A", "Z", BigDecimal.TEN))
            rateService.addRate(Rate("A", "D", BigDecimal.TEN))
            rateService.addRate(Rate("B", "Z", BigDecimal.TEN))
            rateService.addRate(Rate("Z", "C", BigDecimal.TEN))
            rateService.addRate(Rate("Z", "D", BigDecimal.TEN))
            rateService.addTransitiveSymbols(Symbols(listOf("Z", "D")))

            Assertions.assertEquals(2, graphService.getAvailableRoutes().size)
            rateService.deleteTransitiveSymbols(Symbols(listOf("Z", "D")))
            Assertions.assertEquals(8, graphService.getAvailableRoutes().size)
        }
    }

    private suspend fun addCurrency(c: String) {
        currencyService.createNewCurrency(CurrencyCommand(symbol =  c,name= c, precision = BigDecimal.ONE))
    }
}