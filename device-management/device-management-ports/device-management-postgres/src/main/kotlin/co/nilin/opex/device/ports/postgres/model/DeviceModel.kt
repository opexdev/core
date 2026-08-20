package co.nilin.opex.device.ports.postgres.model

import co.nilin.opex.device.core.data.Os
import co.nilin.opex.device.core.data.Platform
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime


@Table(name = "devices")
data class DeviceModel(
    @Id
    val id: Long? = null,
    @Column("device_uuid")
    val deviceUuid: String,
    @Column("os")
    val os: Os?,
    @Column("os_version")
    val osVersion: String?,
    @Column("app_version")
    val appVersion: String?,
    @Column("brand")
    val brand: String?,
    @Column("model")
    val model: String?,
    @Column("platform")
    val platform: Platform?,
    @Column("agent")
    val agent: String?,
    @Column("push_token")
    val pushToken: String?,
    @Column("build_number")
    val buildNumber: Int?,
    @Column("create_date")
    val createDate: LocalDateTime? = LocalDateTime.now(),
    @Column("last_update_date")
    val lastUpdateDate: LocalDateTime? = LocalDateTime.now(),
    @Version
    @Column("version")
    var version: Long? = null
)
