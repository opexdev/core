package co.nilin.opex.device.core.data

import java.time.LocalDateTime


data class Device(
    val id: Long? = null,
    val deviceUuid: String,
    val os: Os? = null,
    val osVersion: String? = null,
    val appVersion: String? = null,
    val brand: String? = null,
    val model: String? = null,
    val platform: Platform? = null,
    val agent: String? = null,
    val pushToken: String? = null,
    val buildNumber: Int? = null,
    val createDate: LocalDateTime? = LocalDateTime.now(),
    val lastUpdateDate: LocalDateTime? = LocalDateTime.now()
)
