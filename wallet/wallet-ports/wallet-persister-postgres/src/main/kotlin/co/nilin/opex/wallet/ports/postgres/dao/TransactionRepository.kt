package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.dto.DepositWithdrawTransaction
import co.nilin.opex.wallet.ports.postgres.dto.TransactionStat
import co.nilin.opex.wallet.ports.postgres.dto.TransactionWithDetail
import co.nilin.opex.wallet.ports.postgres.model.TransactionModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface TransactionRepository : ReactiveCrudRepository<TransactionModel, Long> {
    @Query(
        "SELECT count(1) cnt, COALESCE(sum(source_amount), 0) total" +
                " FROM transaction tm " +
                " join wallet wm on wm.id = tm.source_wallet " +
                " WHERE wm.owner = :owner " +
                " and wm.wallet_type = :walletType " +
                " and wm.currency = :currency " +
                " and tm.transaction_date >= :startDate " +
                " and tm.transaction_date <= :endDate"
    )
    fun calculateWithdrawStatisticsBasedOnCurrency(
        @Param("owner") owner: Long,
        @Param("walletType") walletType: String,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime,
        @Param("currency") currency: String
    ): Mono<TransactionStat>

    @Query(
        "SELECT count(1) cnt, COALESCE(sum(source_amount), 0) total " +
                " FROM transaction tm " +
                " join wallet wm on wm.id = tm.source_wallet " +
                " WHERE wm.owner = :owner " +
                " and wm.id = :walletId " +
                " and tm.transaction_date >= :startDate " +
                " and tm.transaction_date <= :endDate"
    )
    fun calculateWithdrawStatistics(
        @Param("owner") owner: Long,
        @Param("walletId") wallet: Long,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): Mono<TransactionStat>

    @Query(
        "SELECT count(1) cnt, COALESCE(sum(dest_amount),0) total " +
                " FROM transaction tm " +
                " join wallet wm on wm.id = tm.dest_wallet " +
                " WHERE wm.owner = :owner " +
                " and wm.wallet_type = :walletType " +
                " and wm.currency = :currency " +
                " and tm.transaction_date >= :startDate " +
                " and tm.transaction_date <= :endDate"
    )
    fun calculateDepositStatisticsBasedOnCurrency(
        @Param("owner") owner: Long,
        @Param("walletType") walletType: String,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime,
        @Param("currency") currency: String
    ): Mono<TransactionStat>

    @Query(
        "SELECT count(1) cnt, COALESCE(sum(dest_amount), 0) total" +
                " FROM transaction tm " +
                " join wallet wm on wm.id = tm.dest_wallet " +
                " WHERE wm.owner = :owner " +
                " and wm.id = :walletId " +
                " and tm.transaction_date >= :startDate " +
                " and tm.transaction_date <= :endDate"
    )
    fun calculateDepositStatistics(
        @Param("owner") owner: Long,
        @Param("walletId") wallet: Long,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): Mono<TransactionStat>

    @Query(
        """
        select distinct t.id, w.currency, w.wallet_type as wallet, t.dest_amount as amount, t.description, t.transfer_ref as ref, t.transaction_date as date
        , t.transfer_category as category, t.transfer_detail_json as detail
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
        @Param("uuid") uuid: String,
        @Param("startTime") startTime: LocalDateTime?,
        @Param("endTime") endTime: LocalDateTime?,
        @Param("limit") limit: Int,
    ): Flux<DepositWithdrawTransaction>

    @Query(
        """
        select distinct t.id, w.currency, w.wallet_type as wallet, t.dest_amount as amount, t.description, t.transfer_ref as ref, t.transaction_date as date
        , t.transfer_category as category, t.transfer_detail_json as detail
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
        @Param("uuid") uuid: String,
        @Param("startTime") startTime: LocalDateTime?,
        @Param("endTime") endTime: LocalDateTime?,
        @Param("limit") limit: Int,
    ): Flux<DepositWithdrawTransaction>

    @Query(
        """
        select distinct t.id, w.currency, w.wallet_type as wallet, t.dest_amount as amount, t.description, t.transfer_ref as ref, t.transaction_date as date
               , t.transfer_category as category, t.transfer_detail_json as detail
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
        @Param("uuid") uuid: String,
        @Param("currency") currency: Long,
        @Param("startTime") startTime: LocalDateTime?,
        @Param("endTime") endTime: LocalDateTime?,
        @Param("limit") limit: Int,
    ): Flux<DepositWithdrawTransaction>

    @Query(
        """
        select distinct t.id, w.currency, w.wallet_type as wallet, t.dest_amount as amount, t.description, t.transfer_ref as ref, t.transaction_date as date
        , t.transfer_category as category, t.transfer_detail_json as detail
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
        @Param("uuid") uuid: String,
        @Param("currency") currency: Long,
        @Param("startTime") startTime: LocalDateTime?,
        @Param("endTime") endTime: LocalDateTime?,
        @Param("limit") limit: Int
    ): Flux<DepositWithdrawTransaction>

    @Query(


        """
            
            select distinct t.id, sw.wallet_type as src_wallet, dw.wallet_type as dest_wallet, swo.uuid as sender_uuid, dwo.uuid as receiver_uuid, sw.currency, t.dest_amount as amount, t.description, t.transfer_ref as ref, t.transaction_date as date
            , t.transfer_category as category, t.transfer_detail_json as detail
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
            
            select distinct t.id, sw.wallet_type as src_wallet, dw.wallet_type as dest_wallet, swo.uuid as sender_uuid, dwo.uuid as receiver_uuid, sw.currency, t.dest_amount as amount, t.description, t.transfer_ref as ref, t.transaction_date as date
            , t.transfer_category as category, t.transfer_detail_json as detail
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
            
            order by date asc
            limit :limit
            offset :offset 
           
            
        """
    )
    fun findTransactionsAsc(
        @Param("uuid") uuid: String,
        @Param("currency") currency: Long?,
        @Param("category") category: String?,
        @Param("startTime") startTime: LocalDateTime?,
        @Param("endTime") endTime: LocalDateTime?,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int,
    ): Flux<TransactionWithDetail>

    @Query(

           """
             select distinct t.id, sw.wallet_type as src_wallet, dw.wallet_type as dest_wallet, swo.uuid as sender_uuid, dwo.uuid as receiver_uuid, sw.currency, t.dest_amount as amount, t.description, t.transfer_ref as ref, t.transaction_date as date
            , t.transfer_category as category, t.transfer_detail_json as detail
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
            
            select distinct t.id, sw.wallet_type as src_wallet, dw.wallet_type as dest_wallet, swo.uuid as sender_uuid, dwo.uuid as receiver_uuid, sw.currency, t.dest_amount as amount, t.description, t.transfer_ref as ref, t.transaction_date as date
            , t.transfer_category as category, t.transfer_detail_json as detail
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
        @Param("uuid") uuid: String,
        @Param("currency") currency: Long?,
        @Param("category") category: String?,
        @Param("startTime") startTime: LocalDateTime?,
        @Param("endTime") endTime: LocalDateTime?,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int,
    ): Flux<TransactionWithDetail>

}