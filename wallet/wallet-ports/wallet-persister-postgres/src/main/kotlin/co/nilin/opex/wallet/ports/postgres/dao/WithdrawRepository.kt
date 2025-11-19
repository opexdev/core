package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.core.inout.TransactionSummary
import co.nilin.opex.wallet.core.inout.WithdrawAdminResponse
import co.nilin.opex.wallet.core.model.WithdrawStatus
import co.nilin.opex.wallet.ports.postgres.model.WithdrawModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface WithdrawRepository : ReactiveCrudRepository<WithdrawModel, Long> {

    @Query("select * from withdraws where withdraw_uuid = :id")
    fun findByWithdrawUuid(id: String): Mono<WithdrawModel>

    @Query(
        """
    select 
        wth.withdraw_uuid as withdraw_id,
        wth.uuid as uuid,
        split_part(wo.title, '|', 2) as owner_name,
        wth.amount as amount,
        wm.currency as currency,
        wth.applied_fee as applied_fee,
        wth.dest_amount as dest_amount,
        wth.dest_symbol as dest_symbol,
        wth.dest_address as dest_address,
        wth.dest_network as dest_network,
        wth.dest_notes as dest_note,
        wth.dest_transaction_ref as dest_transaction_ref,
        wth.status_reason as status_reason,
        wth.status as status,
        wth.applicator as applicator,
        wth.withdraw_type as withdraw_type,
        wth.attachment as attachment,
        wth.create_date as create_date,
        wth.last_update_date as last_update_date,
        wth.transfer_method as transfer_method,
        wth.otp_required as otp_required
    from withdraws wth
        join wallet wm on wm.id = wth.wallet
        join wallet_owner wo on wm.owner = wo.id
    where (:owner is null or wo.uuid = :owner)
      and (:destTxRef is null or wth.dest_transaction_ref = :destTxRef)
      and (:destAddress is null or wth.dest_address = :destAddress)
      and (:currency is null or wm.currency in (:currency))
      and (:startTime is null or wth.create_date > :startTime)
      and (:endTime is null or wth.create_date <= :endTime)
    order by  
        CASE WHEN :ascendingByTime=true THEN wth.create_date END ASC,
        CASE WHEN :ascendingByTime=false THEN wth.create_date END DESC
    offset :offset limit :size;
    """
    )
    fun findByCriteria(
        owner: String?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        ascendingByTime: Boolean? = false,
        offset: Int? = 0,
        size: Int? = 10000
    ): Flow<WithdrawAdminResponse>


    @Query(
        """
    select 
        wth.withdraw_uuid as withdraw_id,
        wth.uuid as uuid,
        split_part(wo.title, '|', 2) as owner_name,
        wth.amount as amount,
        wm.currency as currency,
        wth.applied_fee as applied_fee,
        wth.dest_amount as dest_amount,
        wth.dest_symbol as dest_symbol,
        wth.dest_address as dest_address,
        wth.dest_network as dest_network,
        wth.dest_notes as dest_note,
        wth.dest_transaction_ref as dest_transaction_ref,
        wth.status_reason as status_reason,
        wth.status as status,
        wth.applicator as applicator,
        wth.withdraw_type as withdraw_type,
        wth.attachment as attachment,
        wth.create_date as create_date,
        wth.last_update_date as last_update_date,
        wth.transfer_method as transfer_method,
        wth.otp_required as otp_required
    from withdraws wth
        join wallet wm on wm.id = wth.wallet
        join wallet_owner wo on wm.owner = wo.id
    where (:owner is null or wo.uuid = :owner)
      and (:destTxRef is null or wth.dest_transaction_ref = :destTxRef)
      and (:destAddress is null or wth.dest_address = :destAddress)
      and (:currency is null or wm.currency in (:currency))
      and wth.status in (:status)
      and (:startTime is null or wth.create_date > :startTime)
      and (:endTime is null or wth.create_date <= :endTime)
    order by  
        CASE WHEN :ascendingByTime=true THEN wth.create_date END ASC,
        CASE WHEN :ascendingByTime=false THEN wth.create_date END DESC
    offset :offset limit :size;
    """
    )
    fun findByCriteria(
        owner: String?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        status: List<WithdrawStatus>?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        ascendingByTime: Boolean? = false,
        offset: Int? = 0,
        size: Int? = 10000
    ): Flow<WithdrawAdminResponse>

    @Query(
        """
        select count(*) from withdraws wth  
        join wallet wm on wm.id = wth.wallet    
        join wallet_owner wo on wm.owner = wo.id   
        where ( :owner is null or wo.uuid = :owner)
            and (:destTxRef is null or wth.dest_transaction_ref = :destTxRef) 
            and (:destAddress is null or wth.dest_address = :destAddress) 
            and (:currency is null or wm.currency in (:currency))
        """
    )
    fun countByCriteria(
        owner: String?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
    ): Mono<Long>

    @Query(
        """
        select count(*) from withdraws wth  
        join wallet wm on wm.id = wth.wallet    
        join wallet_owner wo on wm.owner = wo.id   
        where ( :owner is null or wo.uuid = :owner)
            and (:destTxRef is null or wth.dest_transaction_ref = :destTxRef) 
            and (:destAddress is null or wth.dest_address = :destAddress) 
            and (:currency is null or wm.currency in (:currency)) 
            and wth.status in (:status)
        """
    )
    fun countByCriteria(
        owner: String?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        status: List<WithdrawStatus>
    ): Mono<Long>

    @Query("select * from withdraws where wallet = :wallet and transaction_id = :txId")
    fun findByWalletAndTransactionId(wallet: Long, txId: String): Mono<WithdrawModel?>

    @Query(
        """
        select * from withdraws 
        where uuid = :uuid and status != 'REQUESTED'
            and (:currency is null or currency = :currency)
            and (:startTime is null or create_date > :startTime )
            and (:endTime is null or create_date <= :endTime)
        order by  CASE WHEN :ascendingByTime=true THEN create_date END ASC,
                  CASE WHEN :ascendingByTime=false THEN create_date END DESC
        limit :limit
        offset :offset
        """
    )
    fun findWithdrawHistory(
        uuid: String,
        currency: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        ascendingByTime: Boolean,
        limit: Int? = 0,
        offset: Int? = 10000
    ): Flow<WithdrawModel>

    @Query(
        """
        select count(*) from withdraws 
        where uuid = :uuid and status != 'REQUESTED'
            and (:currency is null or currency = :currency)
            and (:startTime is null or create_date > :startTime )
            and (:endTime is null or create_date <= :endTime)
        """
    )
    fun findWithdrawHistoryCount(
        uuid: String,
        currency: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
    ): Mono<Long>


    @Query(
        """
       SELECT currency,
            SUM(amount) AS amount
        FROM withdraws
        WHERE uuid = :uuid and status != 'REQUESTED'
            and (:startTime is null or create_date >= :startTime )
            and (:endTime is null or create_date <= :endTime)
        GROUP BY uuid, currency
        ORDER BY amount DESC
        limit :limit;
   """
    )
    fun getWithdrawSummary(
        uuid: String,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int?,
    ): Flow<TransactionSummary>

}