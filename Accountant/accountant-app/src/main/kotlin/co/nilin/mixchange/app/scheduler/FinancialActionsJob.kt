package co.nilin.mixchange.app.scheduler

import co.nilin.mixchange.accountant.core.api.FinancialActionJobManager
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class FinancialActionsJob(
        val financialActionJobManager: FinancialActionJobManager
) {

    private val log = LoggerFactory.getLogger(FinancialActionsJob::class.java)

    @Scheduled(fixedDelay = 10000)
    fun processFinancialActions() {
        runBlocking {
            try {
                //read unprocessed fa records and call transfer
                financialActionJobManager.processFinancialActions(0, 100)
            } catch (e: Exception) {
                log.error("Job error!", e)
            }
        }
    }

}