package co.nilin.opex.device.core.spi

import co.nilin.opex.device.core.data.Device
import co.nilin.opex.device.core.data.UserDevice
import reactor.core.publisher.Mono

interface DevicePersister {
    suspend fun upsertDevice(userId: Long, device: Device, ip: String?, userAgent: String?): Mono<UserDevice>
    suspend fun findUserDevice(userId: Long, deviceUuid: String): Mono<UserDevice>?
}