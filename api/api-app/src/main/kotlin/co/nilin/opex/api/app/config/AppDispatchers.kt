package co.nilin.opex.api.app.config

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

object AppDispatchers {
    val apiExecutor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    val kafkaExecutor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
}