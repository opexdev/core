package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.core.inout.DepositAdminResponse
import co.nilin.opex.wallet.core.inout.TransactionSummary
import co.nilin.opex.wallet.core.model.DepositStatus
import co.nilin.opex.wallet.ports.postgres.model.DepositModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface DepositRepository : ReactiveCrudRepository<DepositModel, Long> {

    @Query(
        """
        select * from deposits 
        where uuid = :uuid
            and (:currency is null or currency = :currency)
            and (:startTime is null or create_date > :startTime )
            and (:endTime is null or create_date <= :endTime)
            and status in (:status)
        order by  CASE WHEN :ascendingByTime=true THEN create_date END ASC,
                  CASE WHEN :ascendingByTime=false THEN create_date END DESC
        limit :limit
        offset :offset
        """
    )
    fun findDepositHistory(
        uuid: String,
        currency: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int? = 0,
        offset: Int? = 10000,
        ascendingByTime: Boolean? = false,
        status: List<DepositStatus>? = listOf<DepositStatus>(DepositStatus.DONE, DepositStatus.INVALID),
    ): Flow<DepositModel>


    @Query(
        """
    select 
        d.id as id,
        d.uuid as uuid,
        split_part(wo.title, '|', 2) as owner_name,
        d.currency as currency,
        d.amount as amount,
        d.network as network,
        d.note as note,
        d.transaction_ref as transaction_ref,
        d.source_address as source_address,
        d.status as status,
        d.deposit_type as type,
        d.attachment as attachment,
        d.create_date as create_date,
        d.transfer_method as transfer_method
    from deposits d
        join wallet_owner wo on d.uuid = wo.uuid
    where (:owner is null or wo.uuid = :owner)
      and (:sourceAddress is null or d.source_address = :sourceAddress)
      and (:currency is null or d.currency = :currency)
      and (:transactionRef is null or d.transaction_ref = :transactionRef)
      and (:startTime is null or d.create_date > :startTime)
      and (:endTime is null or d.create_date <= :endTime)
    order by  
        CASE WHEN :ascendingByTime=true THEN d.create_date END ASC,
        CASE WHEN :ascendingByTime=false THEN d.create_date END DESC
    offset :offset limit :limit;
    """
    )
    fun findByCriteria(
        owner: String?,
        currency: String?,
        sourceAddress: String?,
        transactionRef: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        ascendingByTime: Boolean? = false,
        offset: Int? = 0,
        limit: Int? = 10000,
    ): Flow<DepositAdminResponse>


    @Query(
        """
    select 
        d.id as id,
        d.uuid as uuid,
        split_part(wo.title, '|', 2) as owner_name,
        d.currency as currency,
        d.amount as amount,
        d.network as network,
        d.note as note,
        d.transaction_ref as transaction_ref,
        d.source_address as source_address,
        d.status as status,
        d.deposit_type as type,
        d.attachment as attachment,
        d.create_date as create_date,
        d.transfer_method as transfer_method
    from deposits d
        join wallet_owner wo on d.uuid = wo.uuid
    where (:owner is null or wo.uuid = :owner)
      and (:sourceAddress is null or d.source_address = :sourceAddress)
      and (:currency is null or d.currency = :currency)
      and (:transactionRef is null or d.transaction_ref = :transactionRef)
      and (:startTime is null or d.create_date > :startTime)
      and (:endTime is null or d.create_date <= :endTime)
      and  (:status is null or status in (:status))
    order by  
        CASE WHEN :ascendingByTime=true THEN d.create_date END ASC,
        CASE WHEN :ascendingByTime=false THEN d.create_date END DESC
    offset :offset limit :limit;
    """
    )
    fun findByCriteria(
        owner: String?,
        currency: String?,
        sourceAddress: String?,
        transactionRef: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        status: List<DepositStatus>?,
        ascendingByTime: Boolean? = false,
        offset: Int? = 0,
        limit: Int? = 10000,
    ): Flow<DepositAdminResponse>


    @Query(
        """
       SELECT currency,
            SUM(amount) AS amount
        FROM deposits
        WHERE uuid = :uuid
            and (:startTime is null or create_date >= :startTime )
            and (:endTime is null or create_date <= :endTime)
        GROUP BY uuid, currency
        ORDER BY amount DESC
        limit :limit;
   """
    )
    fun getDepositSummary(
        uuid: String,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int?,
    ): Flow<TransactionSummary>


    @Query(
        """
        select count(*) from deposits 
        where uuid = :uuid
            and (:currency is null or currency = :currency)
            and (:startTime is null or create_date > :startTime )
            and (:endTime is null or create_date <= :endTime)
            and status in (:status)
        """
    )
    fun countByCriteria(
        uuid: String,
        currency: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        status: List<DepositStatus>? = listOf<DepositStatus>(DepositStatus.DONE, DepositStatus.INVALID),
    ): Mono<Long>
}