package co.nilin.opex.wallet.app.service.otc

import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.core.model.otc.ForbiddenPair
import co.nilin.opex.wallet.core.model.otc.Rate
import co.nilin.opex.wallet.core.service.otc.RateService
import co.nilin.opex.wallet.core.spi.CurrencyService
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class GraphService(
    private val rateService: RateService, private val currencyService: CurrencyService
) {
    data class Route(val rates: List<Rate>) {
        fun getSourceSymbol(): String {
            return rates.get(0).sourceSymbol
        }

        fun getDestSymbol(): String {
            return rates.get(rates.lastIndex).destSymbol
        }

        fun getRate(): BigDecimal {
            return rates.map { rate -> rate.rate }.reduce { accumulator, element ->
                accumulator.multiply(element)
            }
        }

    }

    suspend fun buildRoutes(source: String? = null, dest: String? = null): MutableList<Route> {
        val routesWithMax2StepV2: MutableList<Route> = mutableListOf()
        val adjencyMap: Map<String, MutableList<Rate>> = createAdjacencyMapV2()
        val systemCurrencies = currencyService.getCurrencies().currencies
        val vertice: List<String> = systemCurrencies?.filter {
            it.isTransitive == false && it.isActive == true
        }
            ?.map(Currency::symbol)
            ?: throw OpexException(OpexError.NoRecordFound)
        val transitiveSymbols: List<String> = systemCurrencies.filter { it.isTransitive == true }.map(Currency::symbol)
        for (vertex in vertice) {
            if (source == null || vertex == source) {
                val visited = mutableSetOf<String>()
                findRoutesWithMax2EdgesV2(vertex, adjencyMap, visited, 0,
                        mutableListOf(), transitiveSymbols, routesWithMax2StepV2, dest, vertice)
            }
        }
        return routesWithMax2StepV2
    }


    suspend fun getAvailableRoutes(): List<Route> {
        return buildRoutes(null, null)
    }

    suspend fun findRoute(source: String, dest: String): Rate? {
        return buildRoutes(source, dest)
            .map { route -> Rate(route.getSourceSymbol(), route.getDestSymbol(), route.getRate()) }
            .firstOrNull()
    }

    private suspend fun findRoutesWithMax2EdgesV2(
            currentVertex: String,
            adjacencyMap: Map<String, MutableList<Rate>>,
            visited: MutableSet<String>,
            currentLength: Int,
            currentRoute: MutableList<Rate>,
            transitiveSymbols: List<String>,
            routesWithMax2StepV2: MutableList<Route>,
            dest: String? = null,
            availableCurrency:List<String>
    ) {
        if (currentLength == 3) {
            return
        }
        visited.add(currentVertex)
        if (currentLength >= 1) {
            if ((!transitiveSymbols.contains(currentRoute[currentRoute.lastIndex].destSymbol)) &&
                    availableCurrency.contains(currentRoute[currentRoute.lastIndex].destSymbol)) {
                val existingRoute = routesWithMax2StepV2.find { route ->
                    route.getSourceSymbol() == currentRoute[0].sourceSymbol
                            &&
                            route.getDestSymbol() == currentRoute[currentRoute.lastIndex].destSymbol
                }
                if (existingRoute != null) {
                    if (existingRoute.rates.size > currentRoute.size) {
                        routesWithMax2StepV2.remove(existingRoute)
                        addCurrentRouteV2(routesWithMax2StepV2, currentRoute, dest)
                    } else if (existingRoute.rates.size == currentRoute.size
                            && existingRoute.rates != currentRoute
                    ) {
                        throw Exception("Only one route should be available between two symbols")
                    }
                } else {
                    addCurrentRouteV2(routesWithMax2StepV2, currentRoute, dest)
                }
            }
        }

        for (edge in adjacencyMap[currentVertex] ?: emptyList()) {
            if (!visited.contains(edge.destSymbol)) {
                currentRoute.add(edge)
                findRoutesWithMax2EdgesV2(
                        edge.destSymbol,
                        adjacencyMap,
                        visited,
                        currentLength + 1,
                        currentRoute,
                        transitiveSymbols,
                        routesWithMax2StepV2,
                        dest,
                        availableCurrency

                )
                currentRoute.removeAt(currentRoute.size - 1)
            }
        }
        visited.remove(currentVertex)
    }

    private suspend fun addCurrentRouteV2(routesWithMax2Step: MutableList<Route>, currentRoute: MutableList<Rate>, dest: String? = null) {

        val route = Route(currentRoute.toList());
        if ((rateService.getForbiddenPairs().forbiddenPairs?.contains(ForbiddenPair(route.getSourceSymbol(), route.getDestSymbol())) == false)
                && (dest == null || route.getDestSymbol() == dest))
            routesWithMax2Step.add(route)

    }

    private suspend fun createAdjacencyMapV2(): Map<String, MutableList<Rate>> {
        val adjacencyMap = mutableMapOf<String, MutableList<Rate>>()
        rateService.getRate().rates?.forEach { rate ->
            adjacencyMap.computeIfAbsent(rate.sourceSymbol) { mutableListOf() }.add(rate)
        }
        return adjacencyMap
    }

}