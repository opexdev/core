package co.nilin.opex.device.core.spi

import co.nilin.opex.device.core.data.Session
import co.nilin.opex.device.core.data.SessionStatus
import co.nilin.opex.device.core.data.SessionsRequest
import co.nilin.opex.device.core.data.UserSessionDevice
import java.time.LocalDateTime


interface SessionPersister {

    suspend fun createOrUpdateSession(session: Session): Session?

    suspend fun fetchSessionByState(sessionState: String): Session?

    suspend fun fetchUserDeviceSession(sessionsRequest: SessionsRequest): List<UserSessionDevice>

    suspend fun fetchActiveSessions(userId: String): List<Session>

    suspend fun updateSessionStatus(sessionState: String, status: SessionStatus): Boolean

    suspend fun findExpiredSessions(before: LocalDateTime): List<Session>

    suspend fun logoutSessionByState(userId: String, sessionState: String): Boolean

    suspend fun logoutOtherSessions(userId: String, currentSessionState: String)
    suspend fun markExpiredSessions(): Int

}