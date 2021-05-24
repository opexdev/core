package co.nilin.mixchange.app.scheduler

import co.nilin.mixchange.accountant.core.api.FinancialActionJobManager
import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class FinancialActionsJob(
    val financialActionJobManager: FinancialActionJobManager
) {

    @Scheduled(fixedDelay = 10000)
    fun processFinancialActions() {
        runBlocking {
            //read unprocessed fa records and call transfer
            financialActionJobManager.processFinancialActions(0, 100)
        }
    }

}