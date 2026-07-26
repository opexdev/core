package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.core.inout.TransactionSummary
import co.nilin.opex.wallet.core.model.UserTransactionCategory
import co.nilin.opex.wallet.core.model.UserTransactionHistory
import co.nilin.opex.wallet.ports.postgres.model.UserTransactionModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface UserTransactionRepository : ReactiveCrudRepository<UserTransactionModel, Long> {

    @Query(
        """
        select ut.uuid as id, o.uuid as user_id,split_part(o.title, '|', 2) as owner_name, currency, balance, balance_change, category, description, date 
        from user_transaction ut
        join wallet_owner o on o.id = ut.owner_id
        where (:userId is null or o.uuid = :userId)
            and (:currency is null or currency = :currency)
            and (:category is null or category = :category)
            and (:startTime is null or date > :startTime)
            and (:endTime is null or date <= :endTime)
        order by date
        limit :limit
        offset :offset
    """
    )
    fun findUserTransactionHistoryAsc(
        userId: String?,
        currency: String?,
        category: UserTransactionCategory?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int,
    ): Flux<UserTransactionHistory>

    @Query(
        """
        select ut.uuid as id, o.uuid as user_id,split_part(o.title, '|', 2) as owner_name, currency, balance, balance_change, category, description, date
        from user_transaction ut
        join wallet_owner o on o.id = ut.owner_id
        where (:userId is null or o.uuid = :userId)
            and (:currency is null or currency = :currency)
            and (:category is null or category = :category)
            and (:startTime is null or date > :startTime)
            and (:endTime is null or date <= :endTime)
        order by date desc
        limit :limit
        offset :offset
    """
    )
    fun findUserTransactionHistoryDesc(
        userId: String?,
        currency: String?,
        category: UserTransactionCategory?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int,
    ): Flux<UserTransactionHistory>


    @Query("""
        SELECT
            ut.currency,
            SUM(ABS(ut.balance_change)) AS amount
        FROM user_transaction ut
        inner join wallet_owner wo on ut.owner_id = wo.id
        where category = 'TRADE'
        and wo.uuid = :uuid
        and (:startTime is null or date >= :startTime )
        and (:endTime is null or date <= :endTime)
        GROUP BY ut.owner_id, ut.currency
        ORDER BY amount desc
        limit :limit;
    """)
    fun getTradeTransactionSummary(
        uuid: String,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int?,
    ): Flow<TransactionSummary>


    @Query(
        """
        select count(*)
        from user_transaction ut
        join wallet_owner o on o.id = ut.owner_id
        where (:userId is null or o.uuid = :userId)
            and (:currency is null or currency = :currency)
            and (:category is null or category = :category)
            and (:startTime is null or date > :startTime)
            and (:endTime is null or date <= :endTime)
    """
    )
    fun countByCriteria(
        userId: String?,
        currency: String?,
        category: UserTransactionCategory?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?
    ): Mono<Long>
}