package co.nilin.opex.device.app.listener


import co.nilin.opex.device.core.data.LogoutEvent
import co.nilin.opex.device.core.service.SessionService
import co.nilin.opex.device.core.spi.LogoutEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


@Component
class LogoutListener(private val sessionService: SessionService) : LogoutEventListener {


    private val logger = LoggerFactory.getLogger(LogoutListener::class.java)


    override fun id(): String {
        return "LogoutListener"
    }

    override fun onEvent(
        event: LogoutEvent,
        partition: Int, offset: Long, timestamp: Long, eventId: String
    ) {
        logger.info("==========================================================================")
        logger.info("Incoming logout event: $event")
        logger.info("==========================================================================")
        with(event) {
            CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
                try {
                    sessionService.logoutSession(event)
                    logger.info("Session terminated successfully ")
                } catch (ex: Exception) {
                    logger.error("Failed to terminate the session", ex)
                }
            }
        }

    }


}