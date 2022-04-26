package co.nilin.opex.referral.app.config

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

object AppDispatchers {
    val kafkaExecutor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
}