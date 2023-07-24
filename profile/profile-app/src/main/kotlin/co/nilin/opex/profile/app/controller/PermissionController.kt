package co.nilin.opex.profile.app.controller

import co.nilin.opex.profile.app.service.PermissionManagement
import co.nilin.opex.profile.core.data.permission.UpdatePermissionRequest
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/v1/profile/permission")

class PermissionController(private var permissionManagement: PermissionManagement) {
    @GetMapping("/{userId}")
    suspend fun revokeAccess(@PathVariable("userId") userId: String,
                             @RequestBody permissionRequest :UpdatePermissionRequest) {
        permissionRequest.userId=userId
        permissionManagement.updatePermission( permissionRequest)
    }
}