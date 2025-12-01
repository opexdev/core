package co.nilin.opex.profile.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.core.data.event.KycLevelUpdatedEvent
import co.nilin.opex.profile.core.data.kyc.KycLevel
import co.nilin.opex.profile.core.data.profile.*
import co.nilin.opex.profile.core.spi.KycLevelUpdatedPublisher
import co.nilin.opex.profile.core.spi.ProfileApprovalRequestPersister
import co.nilin.opex.profile.core.spi.ProfilePersister
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class ProfileApprovalRequestManagement(
    private val profileApprovalRequestPersister: ProfileApprovalRequestPersister,
    private val kycLevelUpdatedPublisher: KycLevelUpdatedPublisher,
    private val profilePersister: ProfilePersister,
) {

    suspend fun getApprovalRequests(request: ProfileApprovalRequestFilter): List<ProfileApprovalAdminResponse> {
        return profileApprovalRequestPersister.getRequests(request)
    }

    suspend fun getApprovalRequestById(id: Long): ProfileApprovalAdminResponse {
        return profileApprovalRequestPersister.getRequestById(id)
    }

    suspend fun getApprovalRequestByUserId(userId: String): ProfileApprovalUserResponse {
        return profileApprovalRequestPersister.getRequestByUserId(userId)
    }

    suspend fun changeRequestStatus(
        id: Long,
        updater: String,
        description: String?,
        status: ProfileApprovalRequestStatus
    ): ProfileApprovalAdminResponse {
        val approvalRequest = profileApprovalRequestPersister.getRequestById(id)

        if (approvalRequest.status != ProfileApprovalRequestStatus.PENDING || status == ProfileApprovalRequestStatus.PENDING) {
            throw OpexError.InvalidProfileApprovalRequestStatus.exception()
        }

        approvalRequest.status = status
        approvalRequest.updateDate = LocalDateTime.now()
        approvalRequest.updater = updater
        approvalRequest.description = description

        val response = profileApprovalRequestPersister.update(approvalRequest)

        when (status) {
            ProfileApprovalRequestStatus.APPROVED -> {
                kycLevelUpdatedPublisher.publish(
                    KycLevelUpdatedEvent(
                        approvalRequest.userId,
                        KycLevel.LEVEL_2,
                        LocalDateTime.now()
                    )
                )
                profilePersister.updateStatus(approvalRequest.userId, ProfileStatus.ADMIN_APPROVED)
            }

            ProfileApprovalRequestStatus.REJECTED -> {
                profilePersister.updateStatus(approvalRequest.userId, ProfileStatus.ADMIN_REJECTED)
            }

            else -> {
                throw OpexError.InvalidProfileApprovalRequestStatus.exception()
            }
        }

        return response
    }
}