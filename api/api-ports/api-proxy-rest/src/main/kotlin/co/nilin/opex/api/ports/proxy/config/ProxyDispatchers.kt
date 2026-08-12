package co.nilin.opex.api.ports.proxy.config

import kotlinx.coroutines.reactor.asCoroutineDispatcher
import reactor.core.scheduler.Schedulers

object ProxyDispatchers {
    private fun envInt(name: String, default: Int): Int {
        val value = System.getenv(name)?.toIntOrNull() ?: return default
        return if (value > 0) value else default
    }

    private val cpu = Runtime.getRuntime().availableProcessors().coerceAtLeast(4)
    private val defaultThreads = cpu * 4
    private val defaultQueue = 10_000

    val general = Schedulers.newBoundedElastic(
        envInt("API_PROXY_GENERAL_THREADS", defaultThreads),
        envInt("API_PROXY_GENERAL_QUEUE", defaultQueue),
        "general"
    ).asCoroutineDispatcher()

    val market = Schedulers.newBoundedElastic(
        envInt("API_PROXY_MARKET_THREADS", defaultThreads),
        envInt("API_PROXY_MARKET_QUEUE", defaultQueue),
        "market"
    ).asCoroutineDispatcher()

    val wallet = Schedulers.newBoundedElastic(
        envInt("API_PROXY_WALLET_THREADS", defaultThreads),
        envInt("API_PROXY_WALLET_QUEUE", defaultQueue),
        "wallet"
    ).asCoroutineDispatcher()
}