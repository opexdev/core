package co.nilin.opex.profile.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.core.data.event.KycLevelUpdatedEvent
import co.nilin.opex.profile.core.data.kyc.KycLevel
import co.nilin.opex.profile.core.data.profile.ProfileApprovalRequestStatus
import co.nilin.opex.profile.core.data.profile.ProfileApprovalResponse
import co.nilin.opex.profile.core.spi.KycLevelUpdatedPublisher
import co.nilin.opex.profile.core.spi.ProfileApprovalRequestPersister
import co.nilin.opex.profile.core.spi.ProfilePersister
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class ProfileApprovalRequestManagement(
    private val profileApprovalRequestPersister: ProfileApprovalRequestPersister,
    private val kycLevelUpdatedPublisher: KycLevelUpdatedPublisher,
    private val profilePersister: ProfilePersister,
) {

    suspend fun getApprovalRequests(status: ProfileApprovalRequestStatus): List<ProfileApprovalResponse> {
        return profileApprovalRequestPersister.getRequests(status)?.toList() ?: emptyList()
    }

    suspend fun getApprovalRequest(id: Long): ProfileApprovalResponse {
        return profileApprovalRequestPersister.getRequestById(id).awaitFirstOrNull()
            ?: throw OpexError.ProfileApprovalRequestNotfound.exception()
    }

    suspend fun approveRequest(id: Long, updater: String, description: String): ProfileApprovalResponse {
        val request = changeRequestStatus(id, updater, ProfileApprovalRequestStatus.APPROVED, description)
        val profile = profilePersister.getProfile(request.profileId)?.awaitFirstOrNull()
            ?: throw OpexError.ProfileNotfound.exception()
        kycLevelUpdatedPublisher.publish(KycLevelUpdatedEvent(profile.userId!!, KycLevel.Level2, LocalDateTime.now()))
        return request
    }

    suspend fun rejectRequest(id: Long, updater: String, description: String): ProfileApprovalResponse {
        return changeRequestStatus(id, updater, ProfileApprovalRequestStatus.REJECTED, description)
    }

    private suspend fun changeRequestStatus(
        id: Long,
        updater: String,
        newStatus: ProfileApprovalRequestStatus,
        description: String
    ): ProfileApprovalResponse {
        var request = (profileApprovalRequestPersister.getRequestById(id).awaitFirstOrNull()
            ?: throw OpexError.ProfileApprovalRequestNotfound.exception())
        if (request.status != ProfileApprovalRequestStatus.PENDING)
            throw OpexError.InvalidProfileApprovalRequestStatus.exception()
        request.apply {
            status = newStatus
            updateDate = LocalDateTime.now()
            this.updater = updater
            this.description = description
        }
        return profileApprovalRequestPersister.update(request)
    }

}