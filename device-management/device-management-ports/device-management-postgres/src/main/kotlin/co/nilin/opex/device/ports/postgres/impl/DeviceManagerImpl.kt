package co.nilin.opex.device.ports.postgres.impl


import co.nilin.opex.device.core.data.Device
import co.nilin.opex.device.core.spi.DevicePersister
import co.nilin.opex.device.ports.postgres.dao.DeviceRepository
import co.nilin.opex.device.ports.postgres.utils.toDto
import co.nilin.opex.device.ports.postgres.utils.toModel

import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class DeviceManagerImpl(
    private val deviceRepository: DeviceRepository
) : DevicePersister {

    private val logger = LoggerFactory.getLogger(DeviceManagerImpl::class.java)

    override suspend fun upsertDevice(device: Device): Device? {
        val existing = deviceRepository.findByDeviceUuid(device.deviceUuid).awaitFirstOrNull()
        val now = LocalDateTime.now()
        val savedModel = if (existing != null) {
            deviceRepository.save(
                existing.copy(
                    os = device.os,
                    osVersion = device.osVersion,
                    appVersion = device.appVersion,
                    pushToken = device.pushToken,
                    lastUpdateDate = now
                )
            ).awaitFirst()
        } else {
            deviceRepository.save(device.toModel()).awaitFirst()
        }

        return savedModel.toDto()
    }

    override suspend fun fetchDeviceByUuid(deviceUuid: String): Device? {
        return deviceRepository.findByDeviceUuid(deviceUuid)
            .awaitFirstOrNull()
            ?.toDto()
    }

    override suspend fun fetchDevicesByIds(deviceIds: List<Long>): List<Device> {
        return deviceRepository.findAllById(deviceIds)
            .map { it.toDto() }
            .collectList()
            .awaitFirst()
    }

    override suspend fun fetchAllDevices(): List<Device> {
        return deviceRepository.findAll()
            .map { it.toDto() }
            .collectList()
            .awaitFirst()
    }


}
