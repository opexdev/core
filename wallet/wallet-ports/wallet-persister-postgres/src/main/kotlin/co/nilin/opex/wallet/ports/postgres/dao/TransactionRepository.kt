package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.core.model.TradeAdminResponse
import co.nilin.opex.wallet.core.model.TransferCategory
import co.nilin.opex.wallet.core.model.WalletType
import co.nilin.opex.wallet.ports.postgres.dto.DepositWithdrawTransaction
import co.nilin.opex.wallet.ports.postgres.dto.TransactionStat
import co.nilin.opex.wallet.ports.postgres.dto.TransactionWithDetail
import co.nilin.opex.wallet.ports.postgres.model.TransactionModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface TransactionRepository : ReactiveCrudRepository<TransactionModel, Long> {
    @Query(
        """
        SELECT count(1) cnt, COALESCE(sum(source_amount), 0) total
        FROM transaction tm
        join wallet wm on wm.id = tm.source_wallet
        WHERE wm.owner = :owner
        and wm.wallet_type = :walletType
        and wm.currency = :currency
        and tm.transaction_date >= :startDate
        and tm.transaction_date <= :endDate
        """
    )
    fun calculateWithdrawStatisticsBasedOnCurrency(
        owner: Long,
        walletType: WalletType,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        currency: String
    ): Mono<TransactionStat>

    @Query(
        """
        SELECT count(1) cnt, COALESCE(sum(source_amount), 0) total
        FROM transaction tm
        join wallet wm on wm.id = tm.source_wallet
        WHERE wm.owner = :owner
        and wm.id = :walletId
        and tm.transaction_date >= :startDate
        and tm.transaction_date <= :endDate
    """
    )
    fun calculateWithdrawStatistics(
        owner: Long,
        walletId: Long,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Mono<TransactionStat>

    @Query(
        """
        SELECT count(1) cnt, COALESCE(sum(dest_amount),0) total
        FROM transaction tm
        join wallet wm on wm.id = tm.dest_wallet
        WHERE wm.owner = :owner
        and wm.wallet_type = :walletType
        and wm.currency = :currency
        and tm.transaction_date >= :startDate
        and tm.transaction_date <= :endDate
    """
    )
    fun calculateDepositStatisticsBasedOnCurrency(
        owner: Long,
        walletType: WalletType,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        currency: String
    ): Mono<TransactionStat>

    @Query(
        """
        SELECT count(1) cnt, COALESCE(sum(dest_amount), 0) total
        FROM transaction tm
        join wallet wm on wm.id = tm.dest_wallet
        WHERE wm.owner = :owner
        and wm.id = :walletId
        and tm.transaction_date >= :startDate
        and tm.transaction_date <= :endDate
    """
    )
    fun calculateDepositStatistics(
        owner: Long,
        walletId: Long,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Mono<TransactionStat>

    @Query(
        """
        select distinct t.id, w.currency, w.wallet_type as wallet, t.dest_amount as amount, t.description, t.transfer_ref as ref, t.transaction_date as date
        , t.transfer_category as category
        from wallet as w
        inner join wallet_owner as wo on (w.owner = wo.id)
        inner join transaction as t on (w.id = t.dest_wallet)
        where t.transfer_ref is not null 
            and wo.uuid = :uuid
            and (:startTime is null or t.transaction_date > :startTime )
            and (:endTime is null or t.transaction_date <= :endTime)
        limit :limit
        """
    )
    suspend fun findDepositTransactionsByUUID(
        uuid: String,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
    ): Flux<DepositWithdrawTransaction>

    @Query(
        """
        select distinct t.id, w.currency, w.wallet_type as wallet, t.dest_amount as amount, t.description, t.transfer_ref as ref, t.transaction_date as date
        , t.transfer_category as category
        from wallet as w
        inner join wallet_owner as wo on (w.owner = wo.id)
        inner join transaction as t on (w.id = t.source_wallet)
        where wo.uuid = :uuid
            and (:startTime is null or t.transaction_date > :startTime )
            and (:endTime is null or t.transaction_date <= :endTime)
        limit :limit
        """
    )
    fun findWithdrawTransactionsByUUID(
        uuid: String,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
    ): Flux<DepositWithdrawTransaction>

    @Query(
        """
        select distinct t.id, w.currency, w.wallet_type as wallet, t.dest_amount as amount, t.description, t.transfer_ref as ref, t.transaction_date as date
               , t.transfer_category as category
        from wallet as w
        inner join wallet_owner as wo on (w.owner = wo.id)
        inner join transaction as t on (w.id = t.dest_wallet)
        where t.transfer_ref is not null 
            and wo.uuid = :uuid 
            and w.currency = :currency
            and (:startTime is null or t.transaction_date > :startTime )
            and (:endTime is null or t.transaction_date <= :endTime)
        limit :limit
        """
    )
    fun findDepositTransactionsByUUIDAndCurrency(
        uuid: String,
        currency: String,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
    ): Flux<DepositWithdrawTransaction>

    @Query(
        """
        select distinct t.id, w.currency, w.wallet_type as wallet, t.dest_amount as amount, t.description, t.transfer_ref as ref, t.transaction_date as date
        , t.transfer_category as category
        from wallet as w
        inner join wallet_owner as wo on (w.owner = wo.id)
        inner join transaction as t on (w.id = t.source_wallet)
        where wo.uuid = :uuid 
            and w.currency = :currency
            and (:startTime is null or t.transaction_date > :startTime )
            and (:endTime is null or t.transaction_date <= :endTime)
        limit :limit
        """
    )
    fun findWithdrawTransactionsByUUIDAndCurrency(
        uuid: String,
        currency: String,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int
    ): Flux<DepositWithdrawTransaction>

    @Query(
        """
        select distinct t.id, sw.wallet_type as src_wallet_type, dw.wallet_type as dest_wallet_type, swo.uuid as sender_uuid, dwo.uuid as receiver_uuid, sw.currency, t.dest_amount as amount, t.description, t.transfer_ref as ref, t.transaction_date as date
        , t.transfer_category as category
        from transaction as t
        inner join wallet as sw on sw.id  = t.source_wallet
        inner join wallet_owner as swo on (sw.owner = swo.id)
        inner join wallet as dw on dw.id  = t.dest_wallet
        inner join wallet_owner as dwo on (dw.owner = dwo.id)
        where :uuid =swo.uuid 
        and (:startTime is null or t.transaction_date > :startTime )
        and (:endTime is null or t.transaction_date <= :endTime)
        and (:category is null or t.transfer_category = :category) 
        and (:currency is null or sw.currency = :currency) 
        
        union 
        
        select distinct t.id, sw.wallet_type as src_wallet_type, dw.wallet_type as dest_wallet_type, swo.uuid as sender_uuid, dwo.uuid as receiver_uuid, sw.currency, t.dest_amount as amount, t.description, t.transfer_ref as ref, t.transaction_date as date
        , t.transfer_category as category
        from transaction as t
        inner join wallet as sw on sw.id  = t.source_wallet
        inner join wallet_owner as swo on (sw.owner = swo.id)
        inner join wallet as dw on dw.id  = t.dest_wallet
        inner join wallet_owner as dwo on (dw.owner = dwo.id)
        where :uuid = dwo.uuid
        and (:startTime is null or t.transaction_date > :startTime )
        and (:endTime is null or t.transaction_date <= :endTime)
        and (:category is null or t.transfer_category = :category) 
        and (:currency is null or sw.currency = :currency) 
    
        limit :limit
        offset :offset 
        """
    )
    fun findTransactionsAsc(
        uuid: String,
        currency: String?,
        category: TransferCategory?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int,
    ): Flux<TransactionWithDetail>

    @Query(
        """
        select distinct t.id, sw.wallet_type as src_wallet_type, dw.wallet_type as dest_wallet_type, swo.uuid as sender_uuid, dwo.uuid as receiver_uuid, sw.currency, t.dest_amount as amount, t.description, t.transfer_ref as ref, t.transaction_date as date
        , t.transfer_category as category
        from transaction as t
        inner join wallet as sw on sw.id  = t.source_wallet
        inner join wallet_owner as swo on (sw.owner = swo.id)
        inner join wallet as dw on dw.id  = t.dest_wallet
        inner join wallet_owner as dwo on (dw.owner = dwo.id)
        where :uuid =swo.uuid 
        and (:startTime is null or t.transaction_date > :startTime )
        and (:endTime is null or t.transaction_date <= :endTime)
        and (:category is null or t.transfer_category = :category) 
        and (:currency is null or sw.currency = :currency) 
        
        union 
        
        select distinct t.id, sw.wallet_type as src_wallet_type, dw.wallet_type as dest_wallet_type, swo.uuid as sender_uuid, dwo.uuid as receiver_uuid, sw.currency, t.dest_amount as amount, t.description, t.transfer_ref as ref, t.transaction_date as date
        , t.transfer_category as category
        from transaction as t
        inner join wallet as sw on sw.id  = t.source_wallet
        inner join wallet_owner as swo on (sw.owner = swo.id)
        inner join wallet as dw on dw.id  = t.dest_wallet
        inner join wallet_owner as dwo on (dw.owner = dwo.id)
        where :uuid = dwo.uuid
        and (:startTime is null or t.transaction_date > :startTime )
        and (:endTime is null or t.transaction_date <= :endTime)
        and (:category is null or t.transfer_category = :category) 
        and (:currency is null or sw.currency = :currency) 
        order by date desc
        limit :limit
        offset :offset 
        """
    )
    fun findTransactionsDesc(
        uuid: String,
        currency: String?,
        category: TransferCategory?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int,
    ): Flux<TransactionWithDetail>

    @Query(
        """
    select distinct 
        t.id as id,
        sw.currency as currency,
        swo.uuid as source_owner_uuid,
        split_part(swo.title, '|', 2) as source_owner_name,
        dwo.uuid as dest_owner_uuid,
        split_part(dwo.title, '|', 2) as dest_owner_name,
        t.dest_amount as amount,
        t.description as description,
        t.transfer_ref as ref,
        t.transaction_date as date,
        t.transfer_category as category
    from transaction t
    inner join wallet sw on sw.id = t.source_wallet
    inner join wallet_owner swo on sw.owner = swo.id
    inner join wallet dw on dw.id = t.dest_wallet
    inner join wallet_owner dwo on dw.owner = dwo.id
    where (t.transfer_category = 'TRADE')
      and  (:startTime is null or t.transaction_date > :startTime)
      and (:endTime is null or t.transaction_date <= :endTime)
      and (:currency is null or sw.currency = :currency)
    order by transaction_date
    limit :limit
    offset :offset
    """
    )
    fun findTradesForAdminAsc(
        currency: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int,
    ): Flux<TradeAdminResponse>

    @Query(
        """
    select distinct 
        t.id as id,
        sw.currency as currency,
        swo.uuid as source_owner_uuid,
        split_part(swo.title, '|', 2) as source_owner_name,
        dwo.uuid as dest_owner_uuid,
        split_part(dwo.title, '|', 2) as dest_owner_name,
        t.dest_amount as amount,
        t.description as description,
        t.transfer_ref as ref,
        t.transaction_date as date,
        t.transfer_category as category
    from transaction t
    inner join wallet sw on sw.id = t.source_wallet
    inner join wallet_owner swo on sw.owner = swo.id
    inner join wallet dw on dw.id = t.dest_wallet
    inner join wallet_owner dwo on dw.owner = dwo.id
    where (t.transfer_category = 'TRADE')
      and  (:startTime is null or t.transaction_date > :startTime)
      and (:endTime is null or t.transaction_date <= :endTime)
      and (:currency is null or sw.currency = :currency)
    order by  transaction_date desc
    limit :limit
    offset :offset
    """
    )
    fun findTradesForAdminDesc(
        currency: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int,
    ): Flux<TradeAdminResponse>

}