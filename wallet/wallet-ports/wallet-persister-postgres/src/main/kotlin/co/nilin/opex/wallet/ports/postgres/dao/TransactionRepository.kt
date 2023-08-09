package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.dto.DepositWithdrawTransaction
import co.nilin.opex.wallet.ports.postgres.dto.TransactionStat
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
        select distinct t.id, w.currency, t.dest_amount as amount, t.description, t.transfer_ref as ref, t.transaction_date as date
        , t.transfer_category as category, t.transfer_detail_json as detail
        from wallet as w
        inner join wallet_owner as wo on (w.owner = wo.id)
        inner join transaction as t on (w.id = t.dest_wallet)
        where t.transfer_ref is not null 
            and wo.uuid = :uuid
            and t.transaction_date > :startTime 
            and t.transaction_date <= :endTime
        limit :limit
        """
    )
    suspend fun findDepositTransactionsByUUID(
        @Param("uuid") uuid: String,
        @Param("startTime") startTime: LocalDateTime,
        @Param("endTime") endTime: LocalDateTime,
        @Param("limit") limit: Int,
    ): Flux<DepositWithdrawTransaction>

    @Query(
        """
        select distinct t.id, w.currency, t.dest_amount as amount, t.description, t.transfer_ref as ref, t.transaction_date as date
        , t.transfer_category as category, t.transfer_detail_json as detail
        from wallet as w
        inner join wallet_owner as wo on (w.owner = wo.id)
        inner join transaction as t on (w.id = t.source_wallet)
        where wo.uuid = :uuid
            and t.transaction_date > :startTime 
            and t.transaction_date <= :endTime
        limit :limit
        """
    )
    suspend fun findWithdrawTransactionsByUUID(
        @Param("uuid") uuid: String,
        @Param("startTime") startTime: LocalDateTime,
        @Param("endTime") endTime: LocalDateTime,
        @Param("limit") limit: Int,
    ): Flux<DepositWithdrawTransaction>

    @Query(
        """
        select distinct t.id, w.currency, t.dest_amount as amount, t.description, t.transfer_ref as ref, t.transaction_date as date
               , t.transfer_category as category, t.transfer_detail_json as detail
        from wallet as w
        inner join wallet_owner as wo on (w.owner = wo.id)
        inner join transaction as t on (w.id = t.dest_wallet)
        where t.transfer_ref is not null 
            and wo.uuid = :uuid 
            and w.currency = :currency
            and t.transaction_date > :startTime 
            and t.transaction_date <= :endTime
        limit :limit
        """
    )
    suspend fun findDepositTransactionsByUUIDAndCurrency(
        @Param("uuid") uuid: String,
        @Param("currency") currency: String,
        @Param("startTime") startTime: LocalDateTime,
        @Param("endTime") endTime: LocalDateTime,
        @Param("limit") limit: Int,
    ): Flux<DepositWithdrawTransaction>

    @Query(
        """
        select distinct t.id, w.currency, t.dest_amount as amount, t.description, t.transfer_ref as ref, t.transaction_date as date
        , t.transfer_category as category, t.transfer_detail_json as detail
        from wallet as w
        inner join wallet_owner as wo on (w.owner = wo.id)
        inner join transaction as t on (w.id = t.source_wallet)
        where wo.uuid = :uuid 
            and w.currency = :currency
            and t.transaction_date > :startTime 
            and t.transaction_date <= :endTime
        limit :limit
        """
    )
    suspend fun findWithdrawTransactionsByUUIDAndCurrency(
        @Param("uuid") uuid: String,
        @Param("currency") currency: String,
        @Param("startTime") startTime: LocalDateTime,
        @Param("endTime") endTime: LocalDateTime,
        @Param("limit") limit: Int,
    ): Flux<DepositWithdrawTransaction>

    @Query(
        """
        select distinct t.id, w.currency, t.dest_amount as amount, t.description, t.transfer_ref as ref, t.transaction_date as date
        , t.transfer_category as category, t.transfer_detail_json as detail
        from wallet as w
        inner join wallet_owner as wo on (w.owner = wo.id)
        inner join transaction as t on w.id in (t.source_wallet, t.dest_wallet)
        where wo.uuid = :uuid
        and t.transaction_date > :startTime 
        and t.transaction_date <= :endTime
        and (:category is null or t.transfer_category = :category) 
        and (:currency is null or w.currency = :currency) 
        limit :limit
        offset :offset
        """
    )
    suspend fun findTransactions(
        @Param("uuid") uuid: String,
        @Param("currency") currency: String?,
        @Param("category") category: String?,
        @Param("startTime") startTime: LocalDateTime,
        @Param("endTime") endTime: LocalDateTime,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int,
    ): Flux<DepositWithdrawTransaction>

}