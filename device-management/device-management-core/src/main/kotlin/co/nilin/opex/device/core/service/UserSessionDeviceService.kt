package co.nilin.opex.device.core.service

import co.nilin.opex.device.core.data.Device
import co.nilin.opex.device.core.data.LoginEvent
import co.nilin.opex.device.core.data.Session
import co.nilin.opex.device.core.data.SessionStatus
import co.nilin.opex.device.core.data.UserSessionDevice
import co.nilin.opex.device.core.spi.DevicePersister
import co.nilin.opex.device.core.spi.SessionPersister
import co.nilin.opex.device.core.spi.UserDevicePersister
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class UserSessionDeviceService(
    private val devicePersister: DevicePersister,
    private val userDevicePersister: UserDevicePersister,
    private val sessionPersister: SessionPersister
) {

    suspend fun getUserSessionsWithDevices(userId: String, lastN: Int = 10): List<UserSessionDevice> = coroutineScope {
        val userDevices = userDevicePersister.fetchUserDevices(userId)
        val sessions = sessionPersister.fetchSessionsByUserId(userId)
            .filter { it.status == SessionStatus.ACTIVE }
            .sortedByDescending { it.createDate }
            .take(lastN)

        val deviceMap = devicePersister.fetchDevicesByIds(userDevices.map { it.deviceId })
            .associateBy { it.id }

        userDevices.mapNotNull { ud ->
            val device = deviceMap[ud.deviceId] ?: return@mapNotNull null
            val session = sessions.find { it.deviceId == ud.deviceId }
            if (session != null) {
                UserSessionDevice(
                    deviceUuid = device.deviceUuid,
                    os = device.os,
                    osVersion = device.osVersion,
                    appVersion = device.appVersion,
                    pushToken = device.pushToken,
                    firstLoginDate = ud.firstLoginDate,
                    lastLoginDate = ud.lastLoginDate,
                    sessionState = session.sessionState,
                    sessionStatus = session.status,
                    sessionCreateDate = session.createDate,
                    sessionExpireDate = session.expireDate
                )
            } else null
        }
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
        sessionPersister.createSession(
            Session(
                sessionState = loginEvent.sessionId,
                userId = loginEvent.uuid,
                deviceId = device.id,
                expireDate = LocalDateTime.now().plusDays(2)
            )
        )
    }

    suspend fun getAllDevicesOfUser(userId: String) = userDevicePersister.fetchUserDevices(userId)
}
