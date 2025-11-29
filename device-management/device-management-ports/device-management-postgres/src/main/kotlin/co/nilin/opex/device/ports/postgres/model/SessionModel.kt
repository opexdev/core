package co.nilin.opex.device.ports.postgres.model

import co.nilin.opex.device.core.data.SessionStatus
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("sessions")
data class SessionModel(
    @Id val id: Long? = null,
    @Column("session_state") val sessionState: String,
    @Column("uuid") val userId: String,
    @Column("device_id") val deviceId: Long,
    @Column("status") val status: SessionStatus,
    @Column("create_date") val createDate: LocalDateTime? = LocalDateTime.now(),
    @Column("expire_date") val expireDate: LocalDateTime? = LocalDateTime.now()
)

