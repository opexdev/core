package co.nilin.opex.device.app.listener


import co.nilin.opex.device.core.data.LoginEvent
import co.nilin.opex.device.core.spi.LoginEventListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


@Component
class LoginListener : LoginEventListener {


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

        }

    }


}