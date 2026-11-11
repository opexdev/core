package co.nilin.opex.device.ports.postgres.dao


import co.nilin.opex.device.ports.postgres.model.UserDeviceModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface UserDeviceRepository : R2dbcRepository<UserDeviceModel, Long> {
    fun findByUserIdAndDeviceId(userId: String, deviceId: Long): Mono<UserDeviceModel>
    fun findByUserId(userId: String): Flux<UserDeviceModel>

    @Query(
        """
    SELECT COUNT(DISTINCT ud.uuid)
    FROM user_devices ud
    WHERE ud.last_login_date >= :from
      AND ud.last_login_date <= :to
"""
    )
    fun countActiveUsers(
        @Param("from") from: LocalDateTime,
        @Param("to") to: LocalDateTime
    ): Mono<Long>
}
