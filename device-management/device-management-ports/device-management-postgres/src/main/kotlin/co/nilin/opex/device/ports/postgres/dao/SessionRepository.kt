package co.nilin.opex.device.ports.postgres.dao



import co.nilin.opex.device.core.data.SessionStatus
import co.nilin.opex.device.ports.postgres.model.SessionModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface SessionRepository : R2dbcRepository<SessionModel, Long> {
    fun findBySessionState(sessionState: String): Mono<SessionModel>
    fun findByUserId(userId: String): Flux<SessionModel>
    fun findByUserIdAndStatus(userId: String, status: SessionStatus): Flux<SessionModel>
    fun findByUserIdAndSessionState(userId: String, sessionState: String): Mono<SessionModel>
    fun findByStatusAndExpireDateBefore(status: SessionStatus, before: LocalDateTime): Flux<SessionModel>
    @Query("update sessions s set s.status='TERMINATED' where s.uuid=:userId and s.sessionId !=:currentSessionState")
    fun logoutAllUserSessionExceptCurrent(userId: String, currentSessionState: String): Mono<Void>

}
