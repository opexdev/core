package co.nilin.opex.device.core.service

import co.nilin.opex.device.core.data.LogoutEvent
import co.nilin.opex.device.core.spi.DevicePersister
import co.nilin.opex.device.core.spi.SessionPersister
import co.nilin.opex.device.core.spi.UserDevicePersister
import org.springframework.stereotype.Service

@Service
class SessionService(
    private val devicePersister: DevicePersister,
    private val userDevicePersister: UserDevicePersister,
    private val sessionPersister: SessionPersister
) {
    suspend fun logoutSession(logoutEvent: LogoutEvent) {
        if (logoutEvent.logoutOthers == false) {
            sessionPersister.logoutSessionByState(logoutEvent.uuid, logoutEvent.sessionId)
        } else {
            sessionPersister.logoutOtherSessions(logoutEvent.uuid, logoutEvent.sessionId)
        }
    }
}
