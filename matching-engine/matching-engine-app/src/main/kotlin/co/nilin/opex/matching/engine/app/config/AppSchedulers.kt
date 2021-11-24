package co.nilin.opex.matching.engine.app.config

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

object AppSchedulers {
    val generalExecutor = Executors.newFixedThreadPool(5).asCoroutineDispatcher()
    val kafkaExecutor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
}