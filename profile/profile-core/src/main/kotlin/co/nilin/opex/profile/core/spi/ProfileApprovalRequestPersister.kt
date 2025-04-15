package co.nilin.opex.profile.core.spi

import co.nilin.opex.profile.core.data.profile.ProfileApprovalRequest
import co.nilin.opex.profile.core.data.profile.ProfileApprovalRequestStatus
import co.nilin.opex.profile.core.data.profile.ProfileApprovalResponse
import kotlinx.coroutines.flow.Flow
import reactor.core.publisher.Mono

interface ProfileApprovalRequestPersister {
    suspend fun save(request: ProfileApprovalRequest) : Mono<ProfileApprovalRequest>
    suspend fun getRequests(status : ProfileApprovalRequestStatus): Flow<ProfileApprovalResponse>?
    suspend fun getRequestById(id: Long): Mono<ProfileApprovalResponse>
    suspend fun update(request: ProfileApprovalResponse) :ProfileApprovalResponse
}