package co.nilin.opex.device.core.data

import java.time.LocalDateTime

/**
 * Represents a device owned/used by a specific user.
 */
data class UserDevice(
    val id: Long? = null,
    val userId: Long,
    val device: Device,
    val trusted: Boolean = false,
    val label: String? = null,
    val lastLoginAt: LocalDateTime? = null,
    val lastIp: String? = null,
    val userAgent: String? = null
)
