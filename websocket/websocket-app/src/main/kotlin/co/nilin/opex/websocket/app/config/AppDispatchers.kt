package co.nilin.opex.websocket.app.config

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

object AppDispatchers {

    val websocketExecutor = Executors.newFixedThreadPool(32).asCoroutineDispatcher()

    val kafkaExecutor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
}