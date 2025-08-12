package co.nilin.opex.profile.ports.postgres.dao

import co.nilin.opex.profile.core.data.profile.ProfileApprovalRequestStatus
import co.nilin.opex.profile.core.data.profile.ProfileApprovalUserResponse
import co.nilin.opex.profile.ports.postgres.model.entity.ProfileApprovalRequestModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface ProfileApprovalRequestRepository : ReactiveCrudRepository<ProfileApprovalRequestModel, Long> {

    fun findByProfileIdAndStatus(
        profileId: Long,
        status: ProfileApprovalRequestStatus
    ): Mono<ProfileApprovalRequestModel>

    @Query("select * from profile_approval_request p where p.status = :status order by create_date desc")
    fun findByStatus(status: ProfileApprovalRequestStatus): Flow<ProfileApprovalRequestModel>?

    @Query("select * from profile_approval_request p where p.profile_id = :profileId")
    fun findByProfileId(profileId: Long): Mono<ProfileApprovalUserResponse>
}