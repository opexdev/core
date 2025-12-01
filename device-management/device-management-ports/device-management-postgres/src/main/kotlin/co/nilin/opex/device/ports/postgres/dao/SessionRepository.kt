package co.nilin.opex.device.ports.postgres.dao


import co.nilin.opex.device.core.data.SessionStatus
import co.nilin.opex.device.core.data.UserSessionDevice
import co.nilin.opex.device.ports.postgres.model.SessionModel
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.data.repository.query.Param
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

    @Query("update sessions set status='TERMINATED' where uuid=:userId and session_state !=:currentSessionState and status='ACTIVE'")
    fun logoutAllUserSessionExceptCurrent(userId: String, currentSessionState: String): Mono<Void>
    fun findAllByStatusAndExpireDateLessThan(
        status: SessionStatus,
        dateTime: LocalDateTime
    ): Flux<SessionModel>

    @Query(
        """
           SELECT 
        d.device_uuid,
        d.os,
        d.os_version,
        d.app_version,
        ud.first_login_date,
        ud.last_login_date,
        s.session_state,
        s.status as session_status,
        s.create_date as session_create_date,
        s.expire_date as session_expire_date
    FROM user_devices ud
    JOIN devices d ON ud.device_id = d.id
    LEFT JOIN sessions s 
        ON s.device_id = ud.device_id AND s.uuid = ud.uuid
    WHERE ud.uuid = :userId
      AND (:os IS NULL OR d.os = :os)
      AND (:status IS NULL OR s.status = :status)
      order by  CASE WHEN :ascendingByTime=true THEN s.create_date END ASC,
                CASE WHEN :ascendingByTime=false THEN s.create_date END DESC
    """
    )
    fun findUserSessionsWithDevices(
        @Param("userId") userId: String,
        @Param("os") os: String?,
        @Param("status") status: String?,
        @Param("ascendingByTime") ascendingByTime: Boolean,
        pageable: Pageable
    ): Flux<UserSessionDevice>
}
