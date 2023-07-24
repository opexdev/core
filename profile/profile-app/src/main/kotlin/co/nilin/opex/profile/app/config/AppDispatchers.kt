package co.nilin.opex.profile.app.config

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

object AppDispatchers {
    val kafkaExecutor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
}