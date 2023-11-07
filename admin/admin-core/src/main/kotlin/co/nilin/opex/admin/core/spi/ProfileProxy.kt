package co.nilin.opex.admin.core.spi

import co.nilin.opex.admin.core.data.ProfileRequest
import co.nilin.opex.admin.core.data.ProfileResponse
import co.nilin.opex.profile.core.data.profile.Profile
import kotlinx.coroutines.flow.Flow
import reactor.core.publisher.Mono

interface ProfileProxy {
    suspend fun getProfile(profileRequest: ProfileRequest): Flow<ProfileResponse>?


}