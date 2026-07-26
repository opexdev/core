package co.nilin.opex.device.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.device.core.data.SessionsRequest
import co.nilin.opex.device.core.data.UserSessionDevice
import co.nilin.opex.device.core.service.DeviceService
import co.nilin.opex.device.core.service.UserSessionDeviceService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/devices")
class DeviceController(
    private val deviceService: DeviceService,
    private val userSessionDeviceService: UserSessionDeviceService) {
    @PostMapping
    suspend fun getLastSessions(
        @RequestBody sessionsRequest: SessionsRequest,
    ): ResponseEntity<List<UserSessionDevice>?> {
        val sessions = userSessionDeviceService.getUserSessionsWithDevices(sessionsRequest)
        return ResponseEntity.ok(sessions)
    }

//    @GetMapping("/user/{userId}/all-devices")
//    suspend fun getAllDevices(@PathVariable userId: String): ResponseEntity<List<Device>> {
//        val userDevices = deviceService.fetchUserDevices(userId)
//        val devices = deviceService.fetchDevicesByIds(userDevices.map { it.deviceId })
//        return ResponseEntity.ok(devices)
//    }

}
