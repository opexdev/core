package co.nilin.opex.profile.app.service

import co.nilin.opex.profile.core.data.permission.UpdatePermissionRequest
import co.nilin.opex.profile.core.spi.PermissionPersister
import org.springframework.stereotype.Component

@Component
class PermissionManagement(private var permissionPersister: PermissionPersister) {
    fun updatePermission(permissionControlRequest: UpdatePermissionRequest) {
        permissionPersister.updatePermission(permissionControlRequest)

    }
}