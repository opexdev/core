package co.nilin.opex.device.ports.postgres.dao


import co.nilin.opex.device.ports.postgres.model.UserDeviceModel
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
@Repository
interface UserDeviceRepository : R2dbcRepository<UserDeviceModel, Long> {
    fun findByUserIdAndDeviceId(userId: String, deviceId: Long): Mono<UserDeviceModel>
    fun findByUserId(userId: String): Flux<UserDeviceModel>
}
