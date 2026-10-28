package co.nilin.opex.profile.ports.postgres.dao

import co.nilin.opex.profile.core.data.profile.BankAccountApprovalAdminResponse
import co.nilin.opex.profile.core.data.profile.BankAccountApprovalRequestStatus
import co.nilin.opex.profile.core.data.profile.BankAccountApprovalUserResponse
import co.nilin.opex.profile.ports.postgres.model.entity.BankAccountApprovalRequestModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface BankAccountApprovalRequestRepository :
    ReactiveCrudRepository<BankAccountApprovalRequestModel, Long> {

    fun findByBankAccountIdAndStatus(
        bankAccountId: Long,
        status: BankAccountApprovalRequestStatus
    ): Mono<BankAccountApprovalRequestModel>

    @Query(
        """
    SELECT
    r.id,
    r.bank_account_id,
    r.user_id,
    r.status,
    r.create_date,
    r.update_date,
    r.updater,
    r.description,
    b.name,
    b.card_number,
    b.iban,
    b.account_number,
    b.bank,
    b.status AS bank_account_status,
    u.first_name,
    u.last_name
    FROM bank_account_approval_request r
    LEFT JOIN bank_account b ON b.id = r.bank_account_id
    LEFT JOIN profile u ON u.user_id = r.user_id
    WHERE (:userId IS NULL OR r.user_id = :userId)
      AND (:status IS NULL OR r.status = :status)
      AND (:createDateFrom IS NULL OR r.create_date >= :createDateFrom)
      AND (:createDateTo IS NULL OR r.create_date <= :createDateTo)
    ORDER BY r.create_date
    LIMIT :limit OFFSET :offset
"""
    )
    fun findByCriteriaAsc(
        userId: String?,
        status: BankAccountApprovalRequestStatus?,
        createDateFrom: LocalDateTime?,
        createDateTo: LocalDateTime?,
        limit: Int,
        offset: Int,
    ): Flow<BankAccountApprovalAdminResponse>

    @Query(
        """
    SELECT
    r.id,
    r.bank_account_id,
    r.user_id,
    r.status,
    r.create_date,
    r.update_date,
    r.updater,
    r.description,
    b.name,
    b.card_number,
    b.iban,
    b.account_number,
    b.bank,
    b.status AS bank_account_status,
    u.first_name,
    u.last_name
    FROM bank_account_approval_request r
    LEFT JOIN bank_account b ON b.id = r.bank_account_id
    LEFT JOIN profile u ON u.user_id = r.user_id
    WHERE (:userId IS NULL OR r.user_id = :userId)
      AND (:status IS NULL OR r.status = :status)
      AND (:createDateFrom IS NULL OR r.create_date >= :createDateFrom)
      AND (:createDateTo IS NULL OR r.create_date <= :createDateTo)
    ORDER BY r.create_date desc
    LIMIT :limit OFFSET :offset
"""
    )
    fun findByCriteriaDesc(
        userId: String?,
        status: BankAccountApprovalRequestStatus?,
        createDateFrom: LocalDateTime?,
        createDateTo: LocalDateTime?,
        limit: Int,
        offset: Int,
    ): Flow<BankAccountApprovalAdminResponse>

    @Query(
        """
    SELECT
    r.id,
    r.bank_account_id,
    r.user_id,
    r.status,
    r.create_date,
    r.update_date,
    r.updater,
    r.description,
    b.name,
    b.card_number,
    b.iban,
    b.account_number,
    b.bank,
    b.status AS bank_account_status,
    u.first_name,
    u.last_name
    FROM bank_account_approval_request r
    LEFT JOIN bank_account b ON b.id = r.bank_account_id
    LEFT JOIN profile u ON u.user_id = r.user_id
    WHERE r.id = :id
"""
    )
    fun findAdminResponseById(id: Long): Mono<BankAccountApprovalAdminResponse>

    @Query(
        """
    select r.bank_account_id, r.status, r.create_date, r.description
    from bank_account_approval_request r
    where r.bank_account_id = :bankAccountId
    order by r.create_date desc
    limit 1
"""
    )
    fun findByBankAccountId(bankAccountId: Long): Mono<BankAccountApprovalUserResponse>
}
