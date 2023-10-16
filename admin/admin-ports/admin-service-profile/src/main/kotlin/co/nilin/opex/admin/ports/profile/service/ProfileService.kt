package co.nilin.opex.admin.ports.profile.service

import co.nilin.opex.admin.core.data.ProfileRequest
import co.nilin.opex.admin.core.data.ProfileResponse
import co.nilin.opex.admin.core.spi.ProfileProxy
import co.nilin.opex.admin.ports.profile.proxy.ProfileProxyImp
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service

@Service
class ProfileService(private val profileProxyImp: ProfileProxyImp) : ProfileProxy {
    override suspend fun getProfile(profileRequest: ProfileRequest): Flow<ProfileResponse>? {
        return profileProxyImp.getProfile(profileRequest)

    }


}