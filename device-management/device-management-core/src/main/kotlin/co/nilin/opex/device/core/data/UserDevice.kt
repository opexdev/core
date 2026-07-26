package co.nilin.opex.device.core.data

import java.time.LocalDateTime


data class UserDevice(
    val userId: String,
    val deviceId: Long,
    val firstLoginDate: LocalDateTime? = null,
    val lastLoginDate: LocalDateTime? = null
)
