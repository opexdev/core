package co.nilin.opex.profile.core.spi

import co.nilin.opex.profile.core.data.profile.ProfileApprovalAdminResponse
import co.nilin.opex.profile.core.data.profile.ProfileApprovalRequest
import co.nilin.opex.profile.core.data.profile.ProfileApprovalRequestFilter
import co.nilin.opex.profile.core.data.profile.ProfileApprovalUserResponse

interface ProfileApprovalRequestPersister {
    suspend fun save(request: ProfileApprovalRequest): ProfileApprovalRequest
    suspend fun getRequests(request: ProfileApprovalRequestFilter): List<ProfileApprovalAdminResponse>
    suspend fun getRequestById(id: Long): ProfileApprovalAdminResponse
    suspend fun getRequestByUserId(userId: String): ProfileApprovalUserResponse
    suspend fun update(request: ProfileApprovalAdminResponse): ProfileApprovalAdminResponse
}