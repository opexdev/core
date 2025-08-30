package co.nilin.opex.profile.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.core.data.event.KycLevelUpdatedEvent
import co.nilin.opex.profile.core.data.kyc.KycLevel
import co.nilin.opex.profile.core.data.profile.ProfileApprovalAdminResponse
import co.nilin.opex.profile.core.data.profile.ProfileApprovalRequestStatus
import co.nilin.opex.profile.core.data.profile.ProfileApprovalUserResponse
import co.nilin.opex.profile.core.data.profile.ProfileStatus
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

    suspend fun getApprovalRequests(status: ProfileApprovalRequestStatus): List<ProfileApprovalAdminResponse> {
        return profileApprovalRequestPersister.getRequests(status)?.toList() ?: emptyList()
    }

    suspend fun getApprovalRequestById(id: Long): ProfileApprovalAdminResponse {
        return profileApprovalRequestPersister.getRequestById(id).awaitFirstOrNull()
            ?: throw OpexError.ProfileApprovalRequestNotfound.exception()
    }

    suspend fun getApprovalRequestByUserId(userId: String): ProfileApprovalUserResponse {
        val profileId = profilePersister.getProfileId(userId)
        return profileApprovalRequestPersister.getRequestByProfileId(profileId).awaitFirstOrNull()
            ?: throw OpexError.ProfileApprovalRequestNotfound.exception()
    }

    suspend fun approveRequest(id: Long, updater: String, description: String?): ProfileApprovalAdminResponse {
        val request = changeRequestStatus(id, updater, ProfileApprovalRequestStatus.APPROVED, description)
        val profileId = profilePersister.getProfile(request.profileId)?.awaitFirstOrNull()?.userId
            ?: throw OpexError.ProfileNotfound.exception()
        kycLevelUpdatedPublisher.publish(KycLevelUpdatedEvent(profileId, KycLevel.LEVEL_2, LocalDateTime.now()))
        profilePersister.updateStatus(profileId, ProfileStatus.ADMIN_APPROVED)
        return request
    }

    suspend fun rejectRequest(id: Long, updater: String, description: String?): ProfileApprovalAdminResponse {
        val request = changeRequestStatus(id, updater, ProfileApprovalRequestStatus.REJECTED, description)
        val profileId = profilePersister.getProfile(request.profileId)?.awaitFirstOrNull()?.userId
            ?: throw OpexError.ProfileNotfound.exception()
        profilePersister.updateStatus(profileId, ProfileStatus.ADMIN_REJECTED)
        return request
    }

    private suspend fun changeRequestStatus(
        id: Long,
        updater: String,
        newStatus: ProfileApprovalRequestStatus,
        description: String?
    ): ProfileApprovalAdminResponse {
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