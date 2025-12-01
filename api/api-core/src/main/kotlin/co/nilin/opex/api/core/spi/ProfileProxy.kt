package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.*

interface ProfileProxy {

    suspend fun getProfiles(token: String, profileRequest: ProfileRequest): List<Profile>
    suspend fun getProfile(token: String, uuid: String): Profile
    suspend fun getProfileHistory(token: String, uuid: String, limit: Int, offset: Int): List<ProfileHistory>
    suspend fun getProfileApprovalRequests(
        token: String,
        request: ProfileApprovalRequestFilter
    ): List<ProfileApprovalAdminResponse>

    suspend fun getProfileApprovalRequest(token: String, requestId: Long): ProfileApprovalAdminResponse
    suspend fun updateProfileApprovalRequest(
        token: String,
        request: UpdateApprovalRequestBody
    ): ProfileApprovalAdminResponse
}