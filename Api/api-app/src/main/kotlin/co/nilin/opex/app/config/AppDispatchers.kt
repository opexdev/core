package co.nilin.opex.app.config

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

object AppDispatchers {
    val apiExecutor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    val kafkaExecutor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
}