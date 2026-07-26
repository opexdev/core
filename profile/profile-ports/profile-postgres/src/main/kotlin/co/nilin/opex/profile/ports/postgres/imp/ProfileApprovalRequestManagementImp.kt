package co.nilin.opex.profile.ports.postgres.imp

import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.core.data.profile.*
import co.nilin.opex.profile.core.spi.ProfileApprovalRequestPersister
import co.nilin.opex.profile.core.utils.convert
import co.nilin.opex.profile.ports.postgres.dao.ProfileApprovalRequestRepository
import co.nilin.opex.profile.ports.postgres.model.entity.ProfileApprovalRequestModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class ProfileApprovalRequestManagementImp(
    private var profileApprovalRequestRepository: ProfileApprovalRequestRepository,
) : ProfileApprovalRequestPersister {

    override suspend fun save(request: ProfileApprovalRequest): ProfileApprovalRequest {
        profileApprovalRequestRepository.findByUserIdAndStatus(
            request.userId,
            ProfileApprovalRequestStatus.PENDING
        ).awaitFirstOrNull()?.let {
            throw OpexError.ProfileApprovalRequestAlreadyExists.exception()
        } ?: run {
            val requestApprovalRequest: ProfileApprovalRequestModel =
                request.convert(ProfileApprovalRequestModel::class.java)
            return profileApprovalRequestRepository.save(requestApprovalRequest).awaitFirst()
                .convert(ProfileApprovalRequest::class.java)
        }
    }

    override suspend fun getRequests(request: ProfileApprovalRequestFilter): List<ProfileApprovalAdminResponse> {
        val createDateFrom = request.createDateFrom?.let {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
        }
        val createDateTo = request.createDateTo?.let {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
        }

        val approvalRequests =
            if (request.ascendingByTime)
                profileApprovalRequestRepository.findByCriteriaAsc(
                    request.userId,
                    request.status,
                    createDateFrom,
                    createDateTo,
                    request.limit,
                    request.offset
                ) else
                profileApprovalRequestRepository.findByCriteriaDesc(
                    request.userId,
                    request.status,
                    createDateFrom,
                    createDateTo,
                    request.limit,
                    request.offset
                )
        return approvalRequests.map { p -> p.convert(ProfileApprovalAdminResponse::class.java) }.toList()
    }

    override suspend fun getRequestById(id: Long): ProfileApprovalAdminResponse {
        return profileApprovalRequestRepository.findById(id).map { p ->
            p.convert(
                ProfileApprovalAdminResponse::class.java
            )
        }.awaitFirstOrNull() ?: throw OpexError.ProfileApprovalRequestNotfound.exception()
    }

    override suspend fun getRequestByUserId(userId: String): ProfileApprovalUserResponse {
        return profileApprovalRequestRepository.findByUserId(userId).map { p ->
            p.convert(
                ProfileApprovalUserResponse::class.java
            )
        }.awaitFirstOrNull() ?: throw OpexError.ProfileApprovalRequestNotfound.exception()
    }

    override suspend fun update(request: ProfileApprovalAdminResponse): ProfileApprovalAdminResponse {
        val requestApprovalRequest: ProfileApprovalRequestModel =
            request.convert(ProfileApprovalRequestModel::class.java)
        profileApprovalRequestRepository.save(requestApprovalRequest).awaitFirst()

        return request
    }
}