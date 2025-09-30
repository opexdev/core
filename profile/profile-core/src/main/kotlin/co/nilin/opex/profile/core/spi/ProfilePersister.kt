package co.nilin.opex.profile.core.spi

import co.nilin.opex.profile.core.data.kyc.KycLevel
import co.nilin.opex.profile.core.data.profile.*
import kotlinx.coroutines.flow.Flow
import reactor.core.publisher.Mono

interface ProfilePersister {

    suspend fun completeProfile(
        id: String,
        data: CompleteProfileRequest,
        mobileIdentityMatch: Boolean?,
        personalIdentityMatch: Boolean?
    ): Mono<Profile>

    suspend fun updateProfileAsAdmin(id: String, data: Profile): Mono<Profile>
    suspend fun createProfile(data: Profile): Mono<Profile>
    suspend fun getProfile(userId: String): Mono<Profile>?
    suspend fun getProfileId(userId: String): Long
    suspend fun getProfile(id: Long): Mono<Profile>?
    suspend fun getAllProfile(offset: Int, size: Int, profileRequest: ProfileRequest): Flow<Profile>?
    suspend fun getHistory(userId: String, offset: Int, size: Int): List<ProfileHistory>
    suspend fun updateUserLevelAndStatus(userId: String, userLevel: KycLevel)
    suspend fun validateEmailForUpdate(userId: String, email: String)
    suspend fun validateMobileForUpdate(userId: String, mobile: String)
    suspend fun updateMobile(userId: String, mobile: String)
    suspend fun updateEmail(userId: String, email: String)
    suspend fun updateStatus(userId: String, status: ProfileStatus)
}

