package co.nilin.opex.device.core.data

import java.time.LocalDateTime


data class Device(
    val id: Long? = null,
    val deviceUuid: String,
    val os: String? = null,
    val osVersion: String? = null,
    val appVersion: String? = null,
    val pushToken: String? = null,
    val createDate: LocalDateTime?= LocalDateTime.now(),
    val lastUpdateDate: LocalDateTime?= LocalDateTime.now()

)
