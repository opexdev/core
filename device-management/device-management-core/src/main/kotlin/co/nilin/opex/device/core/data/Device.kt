package co.nilin.opex.device.core.data

import java.time.LocalDateTime

/**
 * Represents a physical/logical device fingerprint.
 */
data class Device(
    val id: Long? = null,
    val deviceUuid: String,
    val os: String? = null,
    val osVersion: String? = null,
    val appVersion: String? = null,
    val firstSeenAt: LocalDateTime? = null,
    val lastSeenAt: LocalDateTime? = null
)
