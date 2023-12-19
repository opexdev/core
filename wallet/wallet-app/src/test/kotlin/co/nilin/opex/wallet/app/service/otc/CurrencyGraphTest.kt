package co.nilin.opex.wallet.app.service.otc

import co.nilin.opex.wallet.core.model.otc.Rate
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class CurrencyGraphTest {

    var graph: CurrencyGraph = CurrencyGraph()

//    @Test
//    fun givenTwoPossibleRoute_whenAddCurrencyRates_thenShorterRouteIsAvailable() {
//        graph.addCurrencyRate("A", "Z", BigDecimal.TEN)
//        graph.addCurrencyRate("B", "Z", BigDecimal.TEN)
//        graph.addCurrencyRate("Z", "C", BigDecimal.TEN)
//        graph.addCurrencyRate("Z", "D", BigDecimal.TEN)
//        graph.addCurrencyRate("A", "D", BigDecimal.TEN)
//
//        val routes = graph.getAvailableRoutes();
//        assertEquals(8, routes.size)
//        val adRate = graph.findRoute("A", "D")
//        assertEquals(adRate, Rate("A", "D", BigDecimal.TEN))
//        val acRate = graph.findRoute("A", "C")
//        assertEquals(acRate, Rate("A", "C", BigDecimal.valueOf(100)))
//    }
//
//    @Test
//    fun givenGraphWithTwoPossibleRoute_whenRemoveCurrencyRate_thenLongerRouteIsAvailable() {
//        graph.addCurrencyRate("A", "Z", BigDecimal.TEN)
//        graph.addCurrencyRate("A", "D", BigDecimal.TEN)
//        graph.addCurrencyRate("B", "Z", BigDecimal.TEN)
//        graph.addCurrencyRate("Z", "C", BigDecimal.TEN)
//        graph.addCurrencyRate("Z", "D", BigDecimal.TEN)
//
//        graph.removeCurrencyRate("A", "D")
//
//        val routes = graph.getAvailableRoutes();
//        assertEquals(8, routes.size)
//        val adRate = graph.findRoute("A", "D")
//        assertEquals(adRate, Rate("A", "D", BigDecimal.valueOf(100)))
//        val acRate = graph.findRoute("A", "C")
//        assertEquals(acRate, Rate("A", "C", BigDecimal.valueOf(100)))
//
//    }
//
//    @Test
//    fun givenGraph_whenAddForbiddenRateNames_thenNoRouteIsAvailable() {
//        graph.addCurrencyRate("A", "Z", BigDecimal.TEN)
//        graph.addCurrencyRate("A", "D", BigDecimal.TEN)
//        graph.addCurrencyRate("B", "Z", BigDecimal.TEN)
//        graph.addCurrencyRate("Z", "C", BigDecimal.TEN)
//        graph.addCurrencyRate("Z", "D", BigDecimal.TEN)
//
//        graph.addForbiddenRateNames("A", "C")
//
//        val routes = graph.getAvailableRoutes();
//        assertEquals(7, routes.size)
//        val acRate = graph.findRoute("A", "C")
//        assertNull(acRate)
//    }
//
//    @Test
//    fun givenGraph_whenAddForbiddenRateNames_thenExceptionAddingForbiddenRate() {
//        graph.addCurrencyRate("A", "Z", BigDecimal.TEN)
//        graph.addCurrencyRate("A", "C", BigDecimal.TEN)
//        graph.addCurrencyRate("B", "Z", BigDecimal.TEN)
//        graph.addCurrencyRate("Z", "C", BigDecimal.TEN)
//        graph.addCurrencyRate("Z", "D", BigDecimal.TEN)
//
//        graph.addForbiddenRateNames("A", "D")
//
//        val exception = assertThrows(Exception::class.java) {
//            graph.addCurrencyRate("A", "D", BigDecimal.TEN)
//        }
//
//        assertEquals("This source & dest is forbidden", exception.message)
//    }
//
//    @Test
//    fun givenGraphWithForbiddenRate_whenRemoveForbiddenRateNames_thenRouteIsAvailable() {
//        graph.addCurrencyRate("A", "Z", BigDecimal.TEN)
//        graph.addCurrencyRate("A", "D", BigDecimal.TEN)
//        graph.addCurrencyRate("B", "Z", BigDecimal.TEN)
//        graph.addCurrencyRate("Z", "C", BigDecimal.TEN)
//        graph.addCurrencyRate("Z", "D", BigDecimal.TEN)
//        graph.addForbiddenRateNames("A", "C")
//        assertEquals(7, graph.getAvailableRoutes().size)
//
//        graph.removeForbiddenRateNames("A", "C")
//
//        val routes = graph.getAvailableRoutes();
//        assertEquals(8, routes.size)
//        val acRate = graph.findRoute("A", "C")
//        assertEquals(acRate, Rate("A", "C", BigDecimal.valueOf(100)))
//    }
//
//    @Test
//    fun givenGraph_whenAddTransitiveSymbols_thenRoutesExcluded() {
//        graph.addCurrencyRate("A", "Z", BigDecimal.TEN)
//        graph.addCurrencyRate("A", "D", BigDecimal.TEN)
//        graph.addCurrencyRate("B", "Z", BigDecimal.TEN)
//        graph.addCurrencyRate("Z", "C", BigDecimal.TEN)
//        graph.addCurrencyRate("Z", "D", BigDecimal.TEN)
//        assertEquals(8, graph.getAvailableRoutes().size)
//
//        graph.addTransitiveSymbols(listOf("Z"))
//
//        assertEquals(4, graph.getAvailableRoutes().size)
//    }
//
//    @Test
//    fun givenGraphTransitiveSymbols_whenRemoveTransitiveSymbols_thenRouteIncluded() {
//        graph.addCurrencyRate("A", "Z", BigDecimal.TEN)
//        graph.addCurrencyRate("A", "D", BigDecimal.TEN)
//        graph.addCurrencyRate("B", "Z", BigDecimal.TEN)
//        graph.addCurrencyRate("Z", "C", BigDecimal.TEN)
//        graph.addCurrencyRate("Z", "D", BigDecimal.TEN)
//        graph.addTransitiveSymbols(listOf("Z", "D"))
//        assertEquals(2, graph.getAvailableRoutes().size)
//
//        graph.removeTransitiveSymbols(listOf("Z", "D"))
//
//        assertEquals(8, graph.getAvailableRoutes().size)
//    }


}