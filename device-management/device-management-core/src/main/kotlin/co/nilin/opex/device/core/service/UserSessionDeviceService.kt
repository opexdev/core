package co.nilin.opex.device.core.service

import co.nilin.opex.device.core.data.*
import co.nilin.opex.device.core.spi.DevicePersister
import co.nilin.opex.device.core.spi.SessionPersister
import co.nilin.opex.device.core.spi.UserDevicePersister
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserSessionDeviceService(
    private val devicePersister: DevicePersister,
    private val userDevicePersister: UserDevicePersister,
    private val sessionPersister: SessionPersister
) {

    suspend fun getUserSessionsWithDevices(uuid: String, sessionsRequest: SessionsRequest): List<UserSessionDevice> {
        return sessionPersister.fetchUserDeviceSession(uuid, sessionsRequest)
    }

    @Transactional
    suspend fun upsertDevice(loginEvent: LoginEvent) {
        val device = with(loginEvent) {
            devicePersister.upsertDevice(
                Device(
                    deviceUuid = this.deviceUuid ?: UUID.randomUUID().toString(),
                    os = this.os,
                    osVersion = this.osVersion,
                    appVersion = this.appVersion,
                    pushToken = this.pushToken,
                )
            )
        }
        userDevicePersister.linkDeviceToUser(loginEvent.uuid, device?.id!!)
        sessionPersister.createOrUpdateSession(
            Session(
                sessionState = loginEvent.sessionId,
                userId = loginEvent.uuid,
                deviceId = device.id,
                expireDate = loginEvent.expireDate
            )
        )
    }

    suspend fun getAllDevicesOfUser(userId: String) = userDevicePersister.fetchUserDevices(userId)
}
