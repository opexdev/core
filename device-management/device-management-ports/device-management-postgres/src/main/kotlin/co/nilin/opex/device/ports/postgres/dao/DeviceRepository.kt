package co.nilin.opex.device.ports.postgres.dao


import co.nilin.opex.device.ports.postgres.model.DeviceModel
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
@Repository
interface DeviceRepository : R2dbcRepository<DeviceModel, Long> {
    fun findByDeviceUuid(deviceUuid: String): Mono<DeviceModel>
}
