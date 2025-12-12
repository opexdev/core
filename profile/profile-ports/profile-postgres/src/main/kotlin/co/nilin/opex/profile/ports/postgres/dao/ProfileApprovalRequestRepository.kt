package co.nilin.opex.profile.ports.postgres.dao

import co.nilin.opex.profile.core.data.profile.ProfileApprovalAdminResponse
import co.nilin.opex.profile.core.data.profile.ProfileApprovalRequestStatus
import co.nilin.opex.profile.core.data.profile.ProfileApprovalUserResponse
import co.nilin.opex.profile.ports.postgres.model.entity.ProfileApprovalRequestModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import java.time.LocalDateTime

interface ProfileApprovalRequestRepository : ReactiveCrudRepository<ProfileApprovalRequestModel, Long> {

    fun findByUserIdAndStatus(
        userId: String,
        status: ProfileApprovalRequestStatus
    ): Mono<ProfileApprovalAdminResponse>

    @Query(
        """
    SELECT 
    p.id,
    p.user_id,
    p.status,
    p.create_date,
    p.update_date,
    p.updater,
    p.description,
    u.first_name,
    u.last_name
    FROM profile_approval_request p
    LEFT JOIN profile u ON u.user_id = p.user_id 
    WHERE (:userId IS NULL OR p.user_id = :userId)
      AND (:status IS NULL OR p.status = :status)
      AND (:createDateFrom IS NULL OR p.create_date >= :createDateFrom)
      AND (:createDateTo IS NULL OR p.create_date <= :createDateTo)
    ORDER BY create_date 
    LIMIT :limit OFFSET :offset
"""
    )
    fun findByCriteriaAsc(
        userId: String?,
        status: ProfileApprovalRequestStatus?,
        createDateFrom: LocalDateTime?,
        createDateTo: LocalDateTime?,
        limit: Int,
        offset: Int,
    ): Flow<ProfileApprovalAdminResponse>

    @Query(
        """
    SELECT 
    p.id,
    p.user_id,
    p.status,
    p.create_date,
    p.update_date,
    p.updater,
    p.description,
    u.first_name,
    u.last_name
    FROM profile_approval_request p
    LEFT JOIN profile u ON u.user_id = p.user_id
    WHERE (:userId IS NULL OR p.user_id = :userId)
      AND (:status IS NULL OR p.status = :status)
      AND (:createDateFrom IS NULL OR p.create_date >= :createDateFrom)
      AND (:createDateTo IS NULL OR p.create_date <= :createDateTo)
    ORDER BY create_date desc
    LIMIT :limit OFFSET :offset
"""
    )
    fun findByCriteriaDesc(
        userId: String?,
        status: ProfileApprovalRequestStatus?,
        createDateFrom: LocalDateTime?,
        createDateTo: LocalDateTime?,
        limit: Int,
        offset: Int,
    ): Flow<ProfileApprovalRequestModel>

    @Query("select * from profile_approval_request p where p.user_id = :userId order by create_date desc limit 1")
    fun findByUserId(userId: String): Mono<ProfileApprovalUserResponse>
}