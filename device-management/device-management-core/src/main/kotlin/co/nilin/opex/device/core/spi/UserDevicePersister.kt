package co.nilin.opex.device.core.spi

import co.nilin.opex.device.core.data.UserDevice
import java.time.LocalDateTime

interface UserDevicePersister {
    suspend fun linkDeviceToUser(
        userId: String,
        deviceId: Long,
        firstLoginDate: LocalDateTime = LocalDateTime.now()
    ): UserDevice?

    suspend fun fetchUserDevice(userId: String, deviceId: Long): UserDevice?

    suspend fun fetchUserDevices(userId: String): List<UserDevice>

    suspend fun updateLastUsed(
        userId: String,
        deviceId: Long,
        lastLoginDate: LocalDateTime = LocalDateTime.now()
    ): Boolean

    suspend fun unlinkDeviceFromUser(userId: String, deviceId: Long): Boolean
}