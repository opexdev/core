package co.nilin.opex.bcgateway.app.config

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

object AppDispatchers {
    val chainSyncExecutor = Executors.newFixedThreadPool(5).asCoroutineDispatcher()
    val walletSyncExecutor = Executors.newFixedThreadPool(2).asCoroutineDispatcher()
}