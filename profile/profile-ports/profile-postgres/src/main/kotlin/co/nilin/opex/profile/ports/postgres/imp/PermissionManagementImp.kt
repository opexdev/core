package co.nilin.opex.profile.ports.postgres.imp

import co.nilin.opex.profile.core.data.permission.UpdatePermissionRequest
import co.nilin.opex.profile.core.data.profile.Profile
import co.nilin.opex.profile.core.data.profile.RequiredAdminActions
import co.nilin.opex.profile.core.spi.PermissionPersister
import co.nilin.opex.profile.ports.postgres.dao.ProfileRepository
import co.nilin.opex.profile.ports.postgres.dao.PermissionRepository
import co.nilin.opex.profile.ports.postgres.model.entity.ProfileModel
import co.nilin.opex.profile.ports.postgres.utils.convert
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service
class PermissionManagementImp(private  var permissionRepository: PermissionRepository,
                              private var profileRepository: ProfileRepository):PermissionPersister {
    override suspend fun updatePermission(updatePermissionRequest: UpdatePermissionRequest) {
        updatePermissionRequest.userId.let {
            profileRepository.findByUserId(updatePermissionRequest.userId)?.awaitFirstOrNull()
                    ?.let {
                          updatePermissionRequest.actions?.forEach(
                                  permissionRepository.findAllById()
                          )
            }?: throw OpexException(OpexError.UserNotFound)

        }
    }
}