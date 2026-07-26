package co.nilin.opex.device.app.listener


import co.nilin.opex.device.core.service.UserSessionDeviceService
import co.nilin.opex.device.core.data.LoginEvent
import co.nilin.opex.device.core.spi.LoginEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


@Component
class LoginListener(private val userSessionDeviceService: UserSessionDeviceService) : LoginEventListener {


    private val logger = LoggerFactory.getLogger(LoginListener::class.java)


    override fun id(): String {
        return "LoginListener"
    }

    override fun onEvent(
        event: LoginEvent,
        partition: Int, offset: Long, timestamp: Long, eventId: String
    ) {
        logger.info("==========================================================================")
        logger.info("Incoming login event: $event")
        logger.info("==========================================================================")
        with(event) {
            CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
                try {
                    userSessionDeviceService.upsertDevice(event)
                    logger.info("Session created, Device upserted successfully ")
                } catch (ex: Exception) {
                    logger.error("Failed to create the session/ upsert the device", ex)
                }
            }        }

    }


}