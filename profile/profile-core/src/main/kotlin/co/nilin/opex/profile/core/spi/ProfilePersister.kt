package co.nilin.opex.profile.core.spi

import co.nilin.opex.profile.core.data.profile.KycLevel
import co.nilin.opex.profile.core.data.profile.Profile
import co.nilin.opex.profile.core.data.profile.ProfileHistory
import co.nilin.opex.profile.core.data.profile.UpdateProfileRequest

interface ProfilePersister {

    suspend fun updateProfile(id :String,data: UpdateProfileRequest): Profile
    suspend fun updateProfileAsAdmin(id :String,data: Profile): Profile
    suspend fun createProfile(data: Profile): Profile
    suspend fun getProfile(id: String): Profile?
    suspend fun getAllProfile(offset:Int,size:Int): List<Profile>
    suspend fun getHistory(userId:String,offset:Int,size:Int): List<ProfileHistory>

    suspend fun updateUserLevel(userId:String,userLevel:KycLevel)

}

