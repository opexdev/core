package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.Profile
import co.nilin.opex.api.core.inout.ProfileRequest

interface ProfileProxy {

    suspend fun getProfiles(token: String, profileRequest: ProfileRequest, limit: Int, offset: Int): List<Profile>
    suspend fun getProfile(token: String, uuid: String): Profile

}