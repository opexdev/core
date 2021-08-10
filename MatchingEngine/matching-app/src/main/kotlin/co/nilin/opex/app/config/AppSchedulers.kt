package co.nilin.opex.app.config

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

object AppSchedulers {
    val generalExecutor = Executors.newFixedThreadPool(5).asCoroutineDispatcher()
}