package co.nilin.opex.device.ports.postgres.utils


import co.nilin.opex.device.core.data.Device
import co.nilin.opex.device.core.data.Session
import co.nilin.opex.device.core.data.UserDevice
import co.nilin.opex.device.ports.postgres.model.DeviceModel
import co.nilin.opex.device.ports.postgres.model.SessionModel
import co.nilin.opex.device.ports.postgres.model.UserDeviceModel
import java.time.LocalDateTime

fun Device.toModel(): DeviceModel {
    return DeviceModel(
        deviceUuid = deviceUuid,
        os = os,
        osVersion = osVersion,
        appVersion = appVersion,
        pushToken = pushToken,
        creteDate = createDate ?: LocalDateTime.now(),
        lastUpdateDate = lastUpdateDate ?: LocalDateTime.now()
    )
}

fun DeviceModel.toDto(): Device {
    return Device(
        id = id,
        deviceUuid = deviceUuid,
        os = os,
        osVersion = osVersion,
        appVersion = appVersion,
        pushToken = pushToken,
        createDate = creteDate,
        lastUpdateDate = lastUpdateDate
    )
}

fun UserDevice.toModel(): UserDeviceModel {
    return UserDeviceModel(
        userId = userId,
        deviceId = deviceId,
        firstLoginDate = firstLoginDate ?: LocalDateTime.now(),
        lastLoginDate = lastLoginDate ?: LocalDateTime.now()
    )
}

fun UserDeviceModel.toDto(): UserDevice {
    return UserDevice(
        userId = userId,
        deviceId = deviceId,
        firstLoginDate = firstLoginDate,
        lastLoginDate = lastLoginDate
    )
}

fun Session.toModel(): SessionModel {
    return SessionModel(
        sessionState = sessionState,
        userId = userId,
        deviceId = deviceId,
        status = status,
        createDate = createDate ?: LocalDateTime.now(),
        expireDate = expireDate
    )
}

fun SessionModel.toDto(): Session {
    return Session(
        sessionState = sessionState,
        userId = userId,
        deviceId = deviceId,
        status = status,
        createDate = createDate,
        expireDate = expireDate
    )
}
