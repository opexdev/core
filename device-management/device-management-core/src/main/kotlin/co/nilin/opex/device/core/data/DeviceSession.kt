package co.nilin.opex.device.core.data

import java.time.LocalDateTime

/**
 * Represents a user session tied to a device.
 */
data class DeviceSession(
    val id: Long? = null,
    val sessionId: Long,
    val userId: Long,
    val device: Device,
    val loginAt: LocalDateTime,
    val logoutAt: LocalDateTime? = null,
    val active: Boolean = true,
    val ip: String? = null,
    val userAgent: String? = null
)
