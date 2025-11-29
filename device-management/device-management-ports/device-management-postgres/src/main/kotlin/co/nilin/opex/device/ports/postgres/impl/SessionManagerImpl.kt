package co.nilin.opex.device.ports.postgres.impl


import co.nilin.opex.device.core.data.Session
import co.nilin.opex.device.core.data.SessionStatus
import co.nilin.opex.device.core.spi.SessionPersister
import co.nilin.opex.device.ports.postgres.dao.SessionRepository
import co.nilin.opex.device.ports.postgres.utils.toDto
import co.nilin.opex.device.ports.postgres.utils.toModel
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class SessionManagerImpl(
    private val sessionRepository: SessionRepository
) : SessionPersister {

    private val logger = LoggerFactory.getLogger(SessionManagerImpl::class.java)

    override suspend fun createSession(session: Session): Session? {
        val saved = sessionRepository.save(session.toModel()).awaitFirstOrNull()
        return saved?.toDto()
    }

    override suspend fun fetchSessionByState(sessionState: String): Session? {
        return sessionRepository.findBySessionState(sessionState)
            .awaitFirstOrNull()
            ?.toDto()
    }

    override suspend fun fetchSessionsByUserId(userId: String): List<Session> {
        return sessionRepository.findByUserId(userId)
            .map { it.toDto() }
            .collectList()
            .awaitFirst()
    }

    override suspend fun fetchActiveSessions(userId: String): List<Session> {
        return sessionRepository.findByUserIdAndStatus(userId, SessionStatus.ACTIVE)
            .map { it.toDto() }
            .collectList()
            .awaitFirst()
    }

    override suspend fun updateSessionStatus(sessionState: String, status: SessionStatus): Boolean {
        val session = sessionRepository.findBySessionState(sessionState).awaitFirstOrNull()
        return if (session != null) {
            sessionRepository.save(session.copy(status = status)).awaitFirstOrNull()
            true
        } else {
            false
        }
    }

    override suspend fun findExpiredSessions(before: LocalDateTime): List<Session> {
        return sessionRepository.findByStatusAndExpireDateBefore(SessionStatus.ACTIVE, before)
            .map { it.toDto() }
            .collectList()
            .awaitFirst()
    }

    override suspend fun logoutSessionByState(userId: String, sessionState: String): Boolean {
        val session = sessionRepository.findByUserIdAndSessionState(userId, sessionState).awaitFirstOrNull()
        return if (session != null) {
            sessionRepository.save(session.copy(status = SessionStatus.TERMINATED)).awaitFirstOrNull()
            true
        } else {
            false
        }
    }

    override suspend fun logoutOtherSessions(userId: String, currentSessionState: String) {
        sessionRepository.logoutAllUserSessionExceptCurrent(userId, currentSessionState).awaitFirstOrNull()
    }
}
