package co.nilin.opex.device.core.spi

import co.nilin.opex.device.core.data.Device
import co.nilin.opex.device.core.data.UserDevice
import reactor.core.publisher.Mono

interface DevicePersister {

        suspend fun upsertDevice(device: Device): Device?

        suspend fun fetchDeviceByUuid(deviceUuid: String): Device?

        suspend fun fetchDevicesByIds(deviceIds: List<Long>): List<Device>

        suspend fun fetchAllDevices(): List<Device>
}