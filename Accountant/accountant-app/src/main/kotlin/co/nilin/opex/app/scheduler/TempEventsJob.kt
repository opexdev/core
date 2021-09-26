package co.nilin.opex.app.scheduler

import co.nilin.opex.accountant.core.spi.TempEventPersister
import co.nilin.opex.accountant.core.spi.TempEventRepublisher
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
@Profile("scheduled")
class TempEventsJob(
    val tempEventPersister: TempEventPersister,
    val tempEventRepublisher: TempEventRepublisher,
) {

    private val log = LoggerFactory.getLogger(TempEventsJob::class.java)

    @Scheduled(fixedDelay = 1000)
    fun processTempEventJobs() {
        runBlocking {
            try {
                //read unprocessed temp event and call republish
                val tempEvents = tempEventPersister.fetchTempEvents(0, 100)
                if (tempEvents.isNotEmpty()) {
                    tempEventRepublisher.republish(tempEvents.map { event -> event.eventBody })
                    tempEventPersister.removeTempEvents(tempEvents)
                }
            } catch (e: Exception) {
                log.error("Job error!", e)
            }
        }
    }

}