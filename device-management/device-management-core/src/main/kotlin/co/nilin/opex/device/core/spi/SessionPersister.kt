package co.nilin.opex.device.core.spi

import co.nilin.opex.device.core.data.Session
import co.nilin.opex.device.core.data.SessionStatus
import java.time.LocalDateTime


interface SessionPersister {

    suspend fun createSession(session: Session): Session?

    suspend fun fetchSessionByState(sessionState: String): Session?

    suspend fun fetchSessionsByUserId(userId: String): List<Session>

    suspend fun fetchActiveSessions(userId: String): List<Session>

    suspend fun updateSessionStatus(sessionState: String, status: SessionStatus): Boolean

    suspend fun findExpiredSessions(before: LocalDateTime): List<Session>

    suspend fun logoutSessionByState(userId: String, sessionState: String): Boolean

    suspend fun logoutOtherSessions(userId: String, currentSessionState: String)

}