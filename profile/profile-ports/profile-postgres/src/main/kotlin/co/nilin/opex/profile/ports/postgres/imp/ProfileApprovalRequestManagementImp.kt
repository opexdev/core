package co.nilin.opex.profile.ports.postgres.imp

import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.core.data.profile.ProfileApprovalRequest
import co.nilin.opex.profile.core.data.profile.ProfileApprovalRequestStatus
import co.nilin.opex.profile.core.data.profile.ProfileApprovalResponse
import co.nilin.opex.profile.core.spi.ProfileApprovalRequestPersister
import co.nilin.opex.profile.core.utils.convert
import co.nilin.opex.profile.ports.postgres.dao.ProfileApprovalRequestRepository
import co.nilin.opex.profile.ports.postgres.model.entity.ProfileApprovalRequestModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ProfileApprovalRequestManagementImp(
    private var profileApprovalRequestRepository: ProfileApprovalRequestRepository,
) : ProfileApprovalRequestPersister {
    override suspend fun save(request: ProfileApprovalRequest): Mono<ProfileApprovalRequest> {
        profileApprovalRequestRepository.findByProfileIdAndStatus(
            request.profileId,
            ProfileApprovalRequestStatus.PENDING
        ).awaitFirstOrNull()?.let {
            throw OpexError.ProfileApprovalRequestAlreadyExists.exception()
        } ?: run {
            val requestApprovalRequest: ProfileApprovalRequestModel =
                request.convert(ProfileApprovalRequestModel::class.java)
            profileApprovalRequestRepository.save(requestApprovalRequest).awaitFirstOrNull()
            return Mono.just(request)
        }
    }

    override suspend fun getRequests(status: ProfileApprovalRequestStatus): Flow<ProfileApprovalResponse>? {
        return profileApprovalRequestRepository.findByStatus(status)?.map { p ->
            p.convert(
                ProfileApprovalResponse::class.java
            )
        }
    }

    override suspend fun getRequestById(id: Long): Mono<ProfileApprovalResponse> {
        return profileApprovalRequestRepository.findById(id).map { p ->
            p.convert(
                ProfileApprovalResponse::class.java
            )
        }
    }

    override suspend fun update(request: ProfileApprovalResponse): ProfileApprovalResponse {
        val requestApprovalRequest: ProfileApprovalRequestModel =
            request.convert(ProfileApprovalRequestModel::class.java)
        profileApprovalRequestRepository.save(requestApprovalRequest).awaitFirstOrNull()
        return request
    }
}