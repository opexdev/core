package co.nilin.opex.profile.ports.postgres.imp

import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.core.data.profile.ProfileApprovalRequest
import co.nilin.opex.profile.core.data.profile.ProfileApprovalRequestStatus
import co.nilin.opex.profile.core.data.profile.ProfileApprovalAdminResponse
import co.nilin.opex.profile.core.data.profile.ProfileApprovalUserResponse
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

    override suspend fun getRequests(status: ProfileApprovalRequestStatus): Flow<ProfileApprovalAdminResponse>? {
        return profileApprovalRequestRepository.findByStatus(status)?.map { p ->
            p.convert(
                ProfileApprovalAdminResponse::class.java
            )
        }
    }

    override suspend fun getRequestById(id: Long): Mono<ProfileApprovalAdminResponse> {
        return profileApprovalRequestRepository.findById(id).map { p ->
            p.convert(
                ProfileApprovalAdminResponse::class.java
            )
        }
    }

    override suspend fun getRequestByProfileId(profileId: Long): Mono<ProfileApprovalUserResponse> {
        return profileApprovalRequestRepository.findByProfileId(profileId).map { p ->
            p.convert(
                ProfileApprovalUserResponse::class.java
            )
        }    }

    override suspend fun update(request: ProfileApprovalAdminResponse): ProfileApprovalAdminResponse {
        val requestApprovalRequest: ProfileApprovalRequestModel =
            request.convert(ProfileApprovalRequestModel::class.java)
        profileApprovalRequestRepository.save(requestApprovalRequest).awaitFirstOrNull()

        return request
    }
}