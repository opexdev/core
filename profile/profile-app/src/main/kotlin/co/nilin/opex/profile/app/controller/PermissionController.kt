package co.nilin.opex.profile.app.controller

import co.nilin.opex.profile.app.service.PermisssionManagement
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/v1/profile/permission")

class PermissionController(private var permissionManagement: PermisssionManagement) {
    @GetMapping("/{userId}")
    suspend fun revokeAccess(@PathVariable("userId") userId: String,
                             @RequestBody permissionRequest :P)
    {
        return permissionManagement.ch()
}