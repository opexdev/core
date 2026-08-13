package co.nilin.opex.accountant.app.scheduler

import co.nilin.opex.accountant.core.spi.FinancialActionPersister
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
@Profile("scheduled")
class FinancialActionsArchiveJob(
    private val financialActionPersister: FinancialActionPersister
) {
    private val log = LoggerFactory.getLogger(FinancialActionsArchiveJob::class.java)

    @Value("\${app.fi-action.archive.enabled:true}")
    private var enabled: Boolean = true

    @Value("\${app.fi-action.archive.retention-days:30}")
    private var retentionDays: Long = 30

    @Value("\${app.fi-action.archive.batch-size:1000}")
    private var batchSize: Int = 1000

    @Scheduled(fixedDelayString = "\${app.fi-action.archive.fixed-delay-ms:300000}", initialDelay = 60000)
    fun archiveProcessedActions() {
        if (!enabled || batchSize <= 0 || retentionDays <= 0) return

        runBlocking {
            val before = LocalDateTime.now().minusDays(retentionDays)
            val archived = financialActionPersister.archiveProcessedActions(before, batchSize)
            if (archived > 0) {
                log.info("Archived $archived processed financial actions older than $before")
            }
        }
    }
}
