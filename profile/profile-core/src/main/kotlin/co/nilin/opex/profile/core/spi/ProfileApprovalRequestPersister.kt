package co.nilin.opex.profile.core.spi

import co.nilin.opex.profile.core.data.profile.ProfileApprovalRequest
import co.nilin.opex.profile.core.data.profile.ProfileApprovalRequestStatus
import co.nilin.opex.profile.core.data.profile.ProfileApprovalAdminResponse
import co.nilin.opex.profile.core.data.profile.ProfileApprovalUserResponse
import kotlinx.coroutines.flow.Flow
import reactor.core.publisher.Mono

interface ProfileApprovalRequestPersister {
    suspend fun save(request: ProfileApprovalRequest) : Mono<ProfileApprovalRequest>
    suspend fun getRequests(status : ProfileApprovalRequestStatus): Flow<ProfileApprovalAdminResponse>?
    suspend fun getRequestById(id: Long): Mono<ProfileApprovalAdminResponse>
    suspend fun getRequestByProfileId(profileId: Long): Mono<ProfileApprovalUserResponse>
    suspend fun update(request: ProfileApprovalAdminResponse) :ProfileApprovalAdminResponse
}