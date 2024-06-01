package co.nilin.opex.accountant.app.scheduler

import co.nilin.opex.accountant.core.api.FinancialActionJobManager
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
@Profile("scheduled")
class FinancialActionsJob(private val financialActionJobManager: FinancialActionJobManager) {

    private val log = LoggerFactory.getLogger(FinancialActionsJob::class.java)
    private val scope = CoroutineScope(Dispatchers.IO)
    private val retryScope = CoroutineScope(Dispatchers.IO)

    @Scheduled(fixedDelay = 10000, initialDelay = 10000)
    fun processFinancialActions() {
        scope.ensureActive()
        if (!scope.isCompleted())
            return

        scope.launch {
            try {
                //read unprocessed fa records and call transfer
                financialActionJobManager.processFinancialActions(0, 100)
            } catch (e: Exception) {
                log.error("Financial action PROCESS error: ${e.message}")
            }
        }
    }

    @Scheduled(fixedDelay = 2000, initialDelay = 15000)
    fun retryFinancialActions() {
        retryScope.ensureActive()
        if (!retryScope.isCompleted())
            return

        retryScope.launch {
            try {
                financialActionJobManager.retryFinancialActions(10)
            } catch (e: Exception) {
                log.error("Financial action RETRY error: ${e.message}")
            }
        }
    }

    private fun CoroutineScope.isCompleted() = coroutineContext.job.children.all { it.isCompleted }

}