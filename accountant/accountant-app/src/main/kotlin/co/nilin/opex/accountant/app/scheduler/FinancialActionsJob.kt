package co.nilin.opex.accountant.app.scheduler

import co.nilin.opex.accountant.core.api.FinancialActionProcessor
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
@Profile("scheduled")
class FinancialActionsJob(private val financialActionProcessor: FinancialActionProcessor) {

    private val log = LoggerFactory.getLogger(FinancialActionsJob::class.java)
    private val scope = CoroutineScope(Dispatchers.IO)

    @Scheduled(fixedDelay = 10000)
    fun processFinancialActions() {
        scope.ensureActive()
        if (!scope.isCompleted())
            return

        scope.launch {
            try {
                //read unprocessed fa records and call transfer
                financialActionProcessor.batchProcess(0, 100)
            } catch (e: Exception) {
                log.error("Financial action manager unable to batch process", e)
            }
        }
    }

    private fun CoroutineScope.isCompleted() = coroutineContext.job.children.all { it.isCompleted }

}