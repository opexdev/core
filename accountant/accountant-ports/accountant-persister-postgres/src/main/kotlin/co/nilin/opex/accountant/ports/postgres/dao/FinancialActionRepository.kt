package co.nilin.opex.accountant.ports.postgres.dao

import co.nilin.opex.accountant.core.model.FinancialActionStatus
import co.nilin.opex.accountant.ports.postgres.model.FinancialActionModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface FinancialActionRepository : ReactiveCrudRepository<FinancialActionModel, Long> {

    @Query("select * from fi_actions fi where pointer = :ouid and :uuid in (fi.sender, fi.receiver)")
    fun findByOuidAndUserUuid(
        @Param("ouid") ouid: String,
        @Param("uuid") uuid: String,
        paging: Pageable
    ): Flow<FinancialActionModel>

    @Query(
        """
        select exists(
            select 1
            from fi_actions fi
            where fi.sender = :uuid
              and fi.symbol = :symbol
              and fi.event_type = :eventType
              and fi.status <> 'PROCESSED'
        )
        """
    )
    fun existsUnprocessedBySenderAndSymbolAndEventType(
        @Param("uuid") uuid: String,
        @Param("symbol") symbol: String,
        @Param("eventType") eventType: String
    ): Mono<Boolean>

    @Query("select * from fi_actions fi where status != :status")
    fun findByStatusNot(@Param("status") status: String, paging: Pageable): Flow<FinancialActionModel>

    @Query("update fi_actions set status = :status where id = :id")
    fun updateStatus(@Param("id") id: Long, @Param("status") status: FinancialActionStatus): Mono<Int>

    @Query("update fi_actions set status = :status where uuid = :uuid")
    fun updateStatus(uuid: String, status: FinancialActionStatus): Mono<Int>

    @Query("update fi_actions set status = :status where id in (:ids)")
    fun updateBatchStatus(ids: List<Long>, status: FinancialActionStatus): Mono<Int>

    @Query(
        """
        select * from fi_actions fi 
        where status = 'CREATED'
            and (
                parent_id is null
                or exists(
                    select 1 from fi_actions pfi
                    where pfi.id = fi.parent_id and pfi.status = 'PROCESSED'
                )
            )
        order by create_date
    """
    )
    fun findReadyToProcess(of: Pageable): Flow<FinancialActionModel>

    @Query(
        """
        with candidates as (
            select id
            from fi_actions
            where status = 'PROCESSED'
              and create_date < :before
              and not exists (
                select 1 from fi_action_retry far
                where far.fa_id = fi_actions.id and far.is_resolved = false
              )
              and not exists (
                select 1 from fi_actions child
                where child.parent_id = fi_actions.id
                  and child.status <> 'PROCESSED'
              )
            order by create_date
            limit :limit
        ),
        moved_actions as (
            insert into fi_actions_archive (
                id, uuid, parent_id, event_type, pointer, symbol, amount, sender, sender_wallet_type,
                receiver, receiver_wallet_type, agent, ip, create_date, status, category_name
            )
            select fa.id, fa.uuid, fa.parent_id, fa.event_type, fa.pointer, fa.symbol, fa.amount, fa.sender, fa.sender_wallet_type,
                   fa.receiver, fa.receiver_wallet_type, fa.agent, fa.ip, fa.create_date, fa.status, fa.category_name
            from fi_actions fa
                     join candidates c on c.id = fa.id
            on conflict (id) do nothing
            returning id
        ),
        moved_retries as (
            insert into fi_action_retry_archive (id, fa_id, retries, next_run_time, is_resolved, has_given_up)
            select far.id, far.fa_id, far.retries, far.next_run_time, far.is_resolved, far.has_given_up
            from fi_action_retry far
                     join moved_actions ma on ma.id = far.fa_id
            on conflict (id) do nothing
            returning id
        ),
        moved_errors as (
            insert into fi_action_error_archive (id, fa_id, error, message, body, retry_id, date)
            select fae.id, fae.fa_id, fae.error, fae.message, fae.body, fae.retry_id, fae.date
            from fi_action_error fae
                     join moved_actions ma on ma.id = fae.fa_id
            on conflict (id) do nothing
            returning id
        ),
        deleted_errors as (
            delete from fi_action_error fae
                using moved_actions ma
            where fae.fa_id = ma.id
            returning fae.id
        ),
        deleted_retries as (
            delete from fi_action_retry far
                using moved_actions ma
            where far.fa_id = ma.id
            returning far.id
        ),
        deleted_actions as (
            delete from fi_actions fa
                using moved_actions ma
            where fa.id = ma.id
            returning fa.id
        )
        select count(1) from deleted_actions
        """
    )
    fun archiveProcessedActions(
        @Param("before") before: LocalDateTime,
        @Param("limit") limit: Int
    ): Mono<Long>
}