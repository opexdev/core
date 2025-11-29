package co.nilin.opex.device.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime


@Table(name = "devices")
data class DeviceModel(
    @Id val id: Long?=null,
    @Column("device_uuid") val deviceUuid: String,
    @Column("os") val os: String?,
    @Column("os_version") val osVersion: String?,
    @Column("app_version") val appVersion: String?,
    @Column("push_token") val pushToken: String?,
    @Column("create_date") val creteDate: LocalDateTime? = LocalDateTime.now(),
    @Column("last_update_date") val lastUpdateDate: LocalDateTime? = LocalDateTime.now(),
    @Column @Version var version: Long? = null
)
