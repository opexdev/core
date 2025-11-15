package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.Profile
import co.nilin.opex.api.core.inout.ProfileHistory
import co.nilin.opex.api.core.inout.ProfileRequest

interface ProfileProxy {

    suspend fun getProfiles(token: String, profileRequest: ProfileRequest): List<Profile>
    suspend fun getProfile(token: String, uuid: String): Profile
    suspend fun getProfileHistory(token: String, uuid: String, limit: Int, offset: Int): List<ProfileHistory>

}