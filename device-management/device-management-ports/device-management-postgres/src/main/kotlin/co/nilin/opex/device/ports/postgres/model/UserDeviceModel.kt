package co.nilin.opex.device.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime


@Table("user_devices")
data class UserDeviceModel(
    @Id val id: Long? = null,
    @Column("uuid") val userId: String,
    @Column("device_id") val deviceId: Long,
    @Column("first_login_date") val firstLoginDate: LocalDateTime? = LocalDateTime.now(),
    @Column("last_login_date") val lastLoginDate: LocalDateTime? = LocalDateTime.now()
)