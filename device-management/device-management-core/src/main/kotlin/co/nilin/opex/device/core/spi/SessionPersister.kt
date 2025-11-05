package co.nilin.opex.device.core.spi

import co.nilin.opex.device.core.data.DeviceSession
import co.nilin.opex.device.core.data.Device
import reactor.core.publisher.Mono

interface SessionPersister {
    suspend fun createOrActivateSession(
        sessionId: Long,
        userId: Long,
        device: Device
    ): Mono<DeviceSession>

    suspend fun closeSession(sessionId: Long)
}