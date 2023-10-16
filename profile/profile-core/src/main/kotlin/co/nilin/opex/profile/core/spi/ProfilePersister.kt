package co.nilin.opex.profile.core.spi

import co.nilin.opex.profile.core.data.profile.*
import kotlinx.coroutines.flow.Flow
import reactor.core.publisher.Mono

interface ProfilePersister {

    suspend fun updateProfile(id: String, data: UpdateProfileRequest): Mono<Profile>
    suspend fun updateProfileAsAdmin(id: String, data: Profile): Mono<Profile>
    suspend fun createProfile(data: Profile): Mono<Profile>
    suspend fun getProfile(id: String): Mono<Profile>?
    suspend fun getAllProfile(offset: Int, size: Int, profileRequest: ProfileRequest): Flow<Profile>?
    suspend fun getHistory(userId: String, offset: Int, size: Int): List<ProfileHistory>

    suspend fun updateUserLevel(userId: String, userLevel: KycLevel)

}

