package co.nilin.opex.port.websocket.config

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

object AppDispatchers {

    val websocketExecutor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    val kafkaExecutor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
}