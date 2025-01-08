package co.nilin.opex.api.ports.proxy.config

import kotlinx.coroutines.reactor.asCoroutineDispatcher
import reactor.core.scheduler.Schedulers

object ProxySchedulers {

    val general = Schedulers.newBoundedElastic(10, 20, "general").asCoroutineDispatcher()
    val market = Schedulers.newBoundedElastic(30, 60, "market").asCoroutineDispatcher()
    val wallet = Schedulers.newBoundedElastic(10, 20, "wallet").asCoroutineDispatcher()
}