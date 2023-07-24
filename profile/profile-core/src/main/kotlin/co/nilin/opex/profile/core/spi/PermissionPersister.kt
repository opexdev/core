package co.nilin.opex.profile.core.spi

import co.nilin.opex.profile.core.data.permission.UpdatePermissionRequest

interface PermissionPersister {
   suspend fun  updatePermission(updatePermissionRequest: UpdatePermissionRequest)
}