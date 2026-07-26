package co.nilin.opex.api.ports.proxy.config

import kotlinx.coroutines.reactor.asCoroutineDispatcher
import reactor.core.scheduler.Schedulers

object ProxyDispatchers {

    val general = Schedulers.newBoundedElastic(8, 16, "general").asCoroutineDispatcher()
    val market = Schedulers.newBoundedElastic(8, 16, "market").asCoroutineDispatcher()
    val wallet = Schedulers.newBoundedElastic(10, 20, "wallet").asCoroutineDispatcher()
}